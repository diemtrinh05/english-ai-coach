package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.example.englishaicoach.auth.dto.RefreshRequest;
import com.example.englishaicoach.auth.dto.RefreshResponse;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(RefreshIntegrationTests.TimeConfiguration.class)
class RefreshIntegrationTests extends PostgreSqlIntegrationTestSupport {
    static final Instant NOW = Instant.parse("2026-10-10T00:00:00Z");
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired RefreshTokenHasher hasher;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService access;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @DynamicPropertySource static void config(DynamicPropertyRegistry r) {
        byte[] key = new byte[32]; new SecureRandom().nextBytes(key);
        String secret = Base64.getEncoder().encodeToString(key);
        r.add("app.jwt.secret", () -> secret);
        r.add("app.jwt.refresh-token-expiration", () -> "7d");
    }
    User user() { return users.saveAndFlush(User.local(UUID.randomUUID()+"@example.test", "password", "Người học", passwords)); }
    String seed(User user, Instant expiry) {
        String raw = UUID.randomUUID().toString();
        tokens.saveAndFlush(RefreshToken.forRawToken(user.getId(), raw, expiry, "thiết bị", hasher)); return raw;
    }
    String json(String raw) throws Exception { return mapper.writeValueAsString(Map.of("refreshToken", raw)); }
    RefreshResponse refresh(String raw) throws Exception {
        var result = mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(json(raw)))
            .andExpect(status().isOk()).andReturn();
        return mapper.readValue(result.getResponse().getContentAsString(), RefreshResponse.class);
    }
    void reject(String raw, String code) throws Exception {
        mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(json(raw)))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(code));
    }
    @Test void sequentialRotationPreservesExpiryDeviceAndHashOnlyStorage() throws Exception {
        User u=user(); Instant expiry=NOW.plusSeconds(7200); String first=seed(u,expiry);
        RefreshResponse second=refresh(first); RefreshResponse third=refresh(second.refreshToken());
        assertThat(second.refreshToken()).isNotEqualTo(first).isNotEqualTo(third.refreshToken());
        assertThat(second.expiresIn()).isEqualTo(900); assertThat(second.tokenType()).isEqualTo("Bearer");
        assertThat(access.verify(third.accessToken()).userId()).isEqualTo(u.getId());
        var old=tokens.findByTokenHash(hasher.hash(first)).orElseThrow();
        assertThat(old.getRevokedAt()).isEqualTo(NOW); assertThat(old.getLastUsedAt()).isEqualTo(NOW);
        var all=tokens.findByUserId(u.getId()); assertThat(all).hasSize(3);
        assertThat(all).allSatisfy(t->{assertThat(t.getExpiresAt()).isEqualTo(expiry);assertThat(t.getDeviceInfo()).isEqualTo("thiết bị");assertThat(t.getTokenHash()).isNotIn(first,second.refreshToken(),third.refreshToken());});
        reject(first,"AUTH_REFRESH_TOKEN_INVALID"); reject(second.refreshToken(),"AUTH_REFRESH_TOKEN_INVALID");
        assertThat(new RefreshRequest(first).toString()).doesNotContain(first);
        assertThat(third.toString()).doesNotContain(third.accessToken(),third.refreshToken());
    }
    @Test void expiryBoundaryUnknownAndRevokedAreRejectedWithoutMutations() throws Exception {
        User u=user(); String boundary=seed(u,NOW); String expired=seed(u,NOW.minusSeconds(1));
        reject(boundary,"AUTH_REFRESH_TOKEN_EXPIRED"); reject(expired,"AUTH_REFRESH_TOKEN_EXPIRED");
        reject(UUID.randomUUID().toString(),"AUTH_REFRESH_TOKEN_INVALID");
        assertThat(tokens.findByUserId(u.getId())).hasSize(2).allSatisfy(t->{assertThat(t.getRevokedAt()).isNull();assertThat(t.getLastUsedAt()).isNull();});
    }
    @Test void manualLockRejectsButLoginCooldownDoesNotBlockSession() throws Exception {
        User u=user(); String raw=seed(u,NOW.plusSeconds(3600));
        jdbc.update("UPDATE users SET status='LOCKED' WHERE id=?",u.getId()); reject(raw,"AUTH_REFRESH_TOKEN_INVALID");
        assertThat(tokens.findByUserId(u.getId())).hasSize(1); assertThat(tokens.findByTokenHash(hasher.hash(raw)).orElseThrow().getRevokedAt()).isNull();
        jdbc.update("UPDATE users SET status='ACTIVE',locked_until=?,failed_login_attempts=5 WHERE id=?",java.sql.Timestamp.from(NOW.plusSeconds(300)),u.getId());
        refresh(raw); User after=users.findById(u.getId()).orElseThrow();
        assertThat(after.getFailedLoginAttempts()).isEqualTo(5);assertThat(after.getLockedUntil()).isEqualTo(NOW.plusSeconds(300));assertThat(after.getLastLoginAt()).isNull();
    }
    @Test void concurrentReuseHasExactlyOneSuccessAndOne401() throws Exception {
        User u=user(); String raw=seed(u,NOW.plusSeconds(3600)); var pool=Executors.newFixedThreadPool(2); var barrier=new CyclicBarrier(2);
        try {
            Callable<Integer> call=()->{barrier.await(5,TimeUnit.SECONDS);return mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(json(raw))).andReturn().getResponse().getStatus();};
            var a=pool.submit(call);var b=pool.submit(call); assertThat(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS))).containsExactlyInAnyOrder(200,401);
        } finally {pool.shutdownNow();}
        assertThat(tokens.findByUserId(u.getId())).hasSize(2);assertThat(tokens.findByUserId(u.getId()).stream().filter(t->t.getRevokedAt()==null)).hasSize(1);
    }
    @Test void currentDatabaseRoleControlsClaimsAndClientRoleIsIgnored() throws Exception {
        User u=user(); String raw=seed(u,NOW.plusSeconds(3600));jdbc.update("UPDATE users SET role='ADMIN' WHERE id=?",u.getId());
        var result=mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content("{\"refreshToken\":\""+raw+"\",\"role\":\"USER\"}")) .andExpect(status().isOk()).andReturn();
        var response=mapper.readValue(result.getResponse().getContentAsString(),RefreshResponse.class);assertThat(access.verify(response.accessToken()).role()).isEqualTo(UserRole.ADMIN);
    }
    @Test void persistenceFailureRollsBackFlushedOldConsumption() throws Exception {
        User u=user(); String raw=seed(u,NOW.plusSeconds(3600));
        jdbc.execute("CREATE FUNCTION reject_refresh_insert() RETURNS trigger LANGUAGE plpgsql AS 'BEGIN IF NEW.user_id = ''"+u.getId()+"''::uuid THEN RAISE EXCEPTION ''injected persistence failure''; END IF; RETURN NEW; END'");
        jdbc.execute("CREATE TRIGGER reject_refresh_insert BEFORE INSERT ON refresh_tokens FOR EACH ROW EXECUTE FUNCTION reject_refresh_insert()");
        try {
            mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(json(raw))).andExpect(status().isInternalServerError());
            var old=tokens.findByTokenHash(hasher.hash(raw)).orElseThrow();
            assertThat(old.getRevokedAt()).isNull();assertThat(old.getLastUsedAt()).isNull();assertThat(tokens.findByUserId(u.getId())).hasSize(1);
        } finally {jdbc.execute("DROP TRIGGER reject_refresh_insert ON refresh_tokens");jdbc.execute("DROP FUNCTION reject_refresh_insert()");}
    }
    @Test void distinctTokenRotationsAndLoginDoNotDeadlockUserLockOrder() throws Exception {
        User u=user(); String first=seed(u,NOW.plusSeconds(3600));String second=seed(u,NOW.plusSeconds(3600));
        var pool=Executors.newFixedThreadPool(3);var barrier=new CyclicBarrier(3);
        try {
            Callable<Integer> a=()->{barrier.await(5,TimeUnit.SECONDS);return mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(json(first))).andReturn().getResponse().getStatus();};
            Callable<Integer> b=()->{barrier.await(5,TimeUnit.SECONDS);return mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(json(second))).andReturn().getResponse().getStatus();};
            Callable<Integer> c=()->{barrier.await(5,TimeUnit.SECONDS);return mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(mapper.writeValueAsString(Map.of("email",u.getEmail(),"password","password")))).andReturn().getResponse().getStatus();};
            var fa=pool.submit(a);var fb=pool.submit(b);var fc=pool.submit(c);assertThat(List.of(fa.get(15,TimeUnit.SECONDS),fb.get(15,TimeUnit.SECONDS),fc.get(15,TimeUnit.SECONDS))).containsOnly(200);
        } finally {pool.shutdownNow();}
        assertThat(tokens.findByUserId(u.getId())).hasSize(5);
    }
    @Test void malformedRequestUsesCanonicalValidationError() throws Exception {
        for(String body:List.of("{}","{\"refreshToken\":\" \"}","{ malformed")) {
            mvc.perform(post("/api/v1/auth/refresh").contentType("application/json").content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }
    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock refreshClock() {return Clock.fixed(NOW,ZoneOffset.UTC);}
    }
}
