package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.config.JwtProperties;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LogoutIntegrationTests.TimeConfiguration.class)
class LogoutIntegrationTests extends PostgreSqlIntegrationTestSupport {
    static final Instant NOW = Instant.parse("2026-10-10T00:00:00Z");
    private static final String SECRET = randomSecret();
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired RefreshTokenHasher hasher;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService access;
    @Autowired JwtProperties properties;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    @DynamicPropertySource static void config(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
        registry.add("app.jwt.refresh-token-expiration", () -> "7d");
    }

    User user() {
        return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học", passwords));
    }

    String seed(User user, Instant expiry) {
        String raw = UUID.randomUUID().toString();
        tokens.saveAndFlush(RefreshToken.forRawToken(user.getId(), raw, expiry, "thiết bị", hasher));
        return raw;
    }

    RefreshToken stored(String raw) {
        return tokens.findByTokenHash(hasher.hash(raw)).orElseThrow();
    }

    MockHttpServletRequestBuilder logout(String jwt, String raw) throws Exception {
        return post("/api/v1/auth/logout").header("Authorization", "Bearer " + jwt)
                .contentType("application/json").content(mapper.writeValueAsString(Map.of("refreshToken", raw)));
    }

    @Test void logoutRevokesOnlySuppliedSessionAndLeavesAccessJwtValid() throws Exception {
        User user = user();
        String raw = seed(user, NOW.plusSeconds(3600));
        String other = seed(user, NOW.plusSeconds(7200));
        String jwt = access.issue(user.getId(), UserRole.USER);
        var response = mvc.perform(logout(jwt, raw)).andExpect(status().isNoContent()).andReturn().getResponse();
        assertThat(response.getContentAsString()).isEmpty();
        assertThat(stored(raw).getRevokedAt()).isEqualTo(NOW);
        assertThat(stored(raw).getLastUsedAt()).isNull();
        assertThat(stored(other).getRevokedAt()).isNull();
        assertThat(stored(raw).getTokenHash()).isNotEqualTo(raw);
        assertThat(access.verify(jwt).userId()).isEqualTo(user.getId());
        mvc.perform(post("/api/v1/auth/refresh").contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("refreshToken", raw))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_REFRESH_TOKEN_INVALID"));
        // Bearer đã cấp vẫn xác thực được cho lần logout kế tiếp.
        mvc.perform(logout(jwt, other)).andExpect(status().isNoContent());
    }

    @Test void expiredAndAlreadyRevokedOwnTokensReturn204WithoutChangingHistoricalTimestamps() throws Exception {
        User user = user();
        String jwt = access.issue(user.getId(), UserRole.USER);
        for (Instant expiry : List.of(NOW, NOW.minusSeconds(1))) {
            String raw = seed(user, expiry);
            mvc.perform(logout(jwt, raw)).andExpect(status().isNoContent());
            assertThat(stored(raw).getRevokedAt()).isEqualTo(NOW);
            Instant past = NOW.minusSeconds(100);
            jdbc.update("UPDATE refresh_tokens SET revoked_at=?,last_used_at=? WHERE token_hash=?",
                    java.sql.Timestamp.from(past), java.sql.Timestamp.from(past), hasher.hash(raw));
            mvc.perform(logout(jwt, raw)).andExpect(status().isNoContent());
            assertThat(stored(raw).getRevokedAt()).isEqualTo(past);
            assertThat(stored(raw).getLastUsedAt()).isEqualTo(past);
        }
        assertThat(tokens.findByUserId(user.getId())).hasSize(2);
    }

    @Test void unknownAndForeignTokensHaveSame401AndNeverChangeAnySession() throws Exception {
        User caller = user(); User owner = user();
        String foreign = seed(owner, NOW.plusSeconds(3600));
        String own = seed(caller, NOW.plusSeconds(3600));
        String jwt = access.issue(caller.getId(), UserRole.ADMIN);
        for (String raw : List.of(UUID.randomUUID().toString(), foreign)) {
            var response = mvc.perform(logout(jwt, raw)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("AUTH_REFRESH_TOKEN_INVALID")).andReturn().getResponse();
            assertThat(response.getContentAsString()).doesNotContain(raw, jwt);
        }
        assertThat(stored(own).getRevokedAt()).isNull();
        assertThat(stored(foreign).getRevokedAt()).isNull();
        jdbc.update("UPDATE refresh_tokens SET revoked_at=? WHERE token_hash=?",
                java.sql.Timestamp.from(NOW.minusSeconds(50)), hasher.hash(foreign));
        mvc.perform(logout(jwt, foreign)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REFRESH_TOKEN_INVALID"));
        assertThat(stored(foreign).getRevokedAt()).isEqualTo(NOW.minusSeconds(50));
    }

    @Test void missingInvalidExpiredAndDuplicateBearerFailBeforeMutation() throws Exception {
        User user = user(); String raw = seed(user, NOW.plusSeconds(3600));
        String jwt = access.issue(user.getId(), UserRole.USER);
        mvc.perform(post("/api/v1/auth/logout").contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("refreshToken", raw))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        String expired = new JwtAccessTokenService(properties,
                Clock.fixed(NOW.minus(Duration.ofMinutes(16)), ZoneOffset.UTC)).issue(user.getId(), UserRole.USER);
        for (String bad : List.of("invalid", expired)) {
            mvc.perform(logout(bad, raw)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
        mvc.perform(logout(jwt, raw).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized());
        assertThat(stored(raw).getRevokedAt()).isNull();
    }

    @Test void malformedOrMissingBodyUsesGlobalValidationWithoutMutation() throws Exception {
        User user = user(); String raw = seed(user, NOW.plusSeconds(3600));
        String jwt = access.issue(user.getId(), UserRole.USER);
        for (String body : List.of("", "{}", "{\"refreshToken\":null}", "{\"refreshToken\":\" \"}", "{ invalid")) {
            mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + jwt)
                    .contentType("application/json").content(body)).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        assertThat(stored(raw).getRevokedAt()).isNull();
    }

    @Test void concurrentLogoutRetriesBothReturn204AndWriteNoNewSession() throws Exception {
        User user = user(); String raw = seed(user, NOW.plusSeconds(3600));
        String jwt = access.issue(user.getId(), UserRole.USER);
        var pool = Executors.newFixedThreadPool(2); var barrier = new CyclicBarrier(2);
        try {
            Callable<Integer> call = () -> { barrier.await(5, TimeUnit.SECONDS);
                return mvc.perform(logout(jwt, raw)).andReturn().getResponse().getStatus(); };
            var first = pool.submit(call); var second = pool.submit(call);
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))).containsOnly(204);
        } finally { pool.shutdownNow(); }
        assertThat(stored(raw).getRevokedAt()).isEqualTo(NOW);
        assertThat(stored(raw).getLastUsedAt()).isNull();
        assertThat(tokens.findByUserId(user.getId())).hasSize(1);
    }

    @Test void concurrentLogoutAndRefreshSerializeAndRespectWinner() throws Exception {
        User user = user(); String raw = seed(user, NOW.plusSeconds(3600));
        String jwt = access.issue(user.getId(), UserRole.USER);
        var pool = Executors.newFixedThreadPool(2); var barrier = new CyclicBarrier(2);
        try {
            var revoked = pool.submit(() -> { barrier.await(5, TimeUnit.SECONDS);
                return mvc.perform(logout(jwt, raw)).andReturn().getResponse().getStatus(); });
            var refreshed = pool.submit(() -> { barrier.await(5, TimeUnit.SECONDS);
                return mvc.perform(post("/api/v1/auth/refresh").contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("refreshToken", raw))))
                        .andReturn().getResponse().getStatus(); });
            assertThat(revoked.get(15, TimeUnit.SECONDS)).isEqualTo(204);
            int refreshStatus = refreshed.get(15, TimeUnit.SECONDS);
            assertThat(refreshStatus).isIn(200, 401);
            assertThat(tokens.findByUserId(user.getId())).hasSize(refreshStatus == 200 ? 2 : 1);
            assertThat(tokens.findByUserId(user.getId()).stream().filter(t -> t.getRevokedAt() == null))
                    .hasSize(refreshStatus == 200 ? 1 : 0);
        } finally { pool.shutdownNow(); }
        assertThat(stored(raw).getRevokedAt()).isEqualTo(NOW);
    }

    @Test void logoutOldTokenAfterCommittedRotationDoesNotRevokeNewToken() throws Exception {
        User user = user(); String raw = seed(user, NOW.plusSeconds(3600));
        String jwt = access.issue(user.getId(), UserRole.USER);
        var result = mvc.perform(post("/api/v1/auth/refresh").contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("refreshToken", raw))))
                .andExpect(status().isOk()).andReturn();
        var rotated = mapper.readValue(result.getResponse().getContentAsString(),
                com.example.englishaicoach.auth.dto.RefreshResponse.class);
        mvc.perform(logout(jwt, raw)).andExpect(status().isNoContent());
        assertThat(stored(raw).getRevokedAt()).isEqualTo(NOW);
        assertThat(stored(raw).getLastUsedAt()).isEqualTo(NOW);
        assertThat(stored(rotated.refreshToken()).getRevokedAt()).isNull();
        assertThat(tokens.findByUserId(user.getId())).hasSize(2);
        mvc.perform(logout(jwt, rotated.refreshToken())).andExpect(status().isNoContent());
        assertThat(stored(rotated.refreshToken()).getRevokedAt()).isEqualTo(NOW);
    }

    @Test void persistenceFailureRollsBackRevokeAndRetryCanSucceed() throws Exception {
        User user = user(); String raw = seed(user, NOW.plusSeconds(3600));
        String jwt = access.issue(user.getId(), UserRole.USER);
        jdbc.execute("CREATE FUNCTION reject_logout_update() RETURNS trigger LANGUAGE plpgsql AS 'BEGIN IF NEW.user_id = ''"
                + user.getId() + "''::uuid THEN RAISE EXCEPTION ''injected logout failure''; END IF; RETURN NEW; END'");
        jdbc.execute("CREATE TRIGGER reject_logout_update BEFORE UPDATE ON refresh_tokens FOR EACH ROW EXECUTE FUNCTION reject_logout_update()");
        try {
            var response = mvc.perform(logout(jwt, raw)).andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR")).andReturn().getResponse();
            assertThat(response.getContentAsString()).doesNotContain(raw, jwt);
            assertThat(stored(raw).getRevokedAt()).isNull();
            assertThat(stored(raw).getLastUsedAt()).isNull();
        } finally {
            jdbc.execute("DROP TRIGGER reject_logout_update ON refresh_tokens");
            jdbc.execute("DROP FUNCTION reject_logout_update()");
        }
        mvc.perform(logout(jwt, raw)).andExpect(status().isNoContent());
    }

    private static String randomSecret() {
        byte[] key = new byte[32]; new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock logoutClock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    }
}
