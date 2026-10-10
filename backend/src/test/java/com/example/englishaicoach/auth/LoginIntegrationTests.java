package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.dto.LoginRequest;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
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
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(LoginIntegrationTests.TimeConfiguration.class)
class LoginIntegrationTests extends PostgreSqlIntegrationTestSupport {
    static final Instant NOW = Instant.parse("2026-10-10T00:00:00Z");
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired RefreshTokenHasher hasher;
    @Autowired PasswordHashService passwords;
    @Autowired JwtAccessTokenService accessTokens;
    @Autowired ObjectMapper mapper;
    @Autowired MutableClock clock;

    @DynamicPropertySource
    static void configuration(DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String secret = Base64.getEncoder().encodeToString(key);
        registry.add("app.jwt.secret", () -> secret);
        registry.add("app.jwt.refresh-token-expiration", () -> "7d");
    }

    @BeforeEach void resetClock() { clock.set(NOW); }

    @Test
    void successReturnsCanonicalTokensAndResetsOnlyCommittedAccountState() throws Exception {
        User user = user("password");
        jdbc.update("UPDATE users SET failed_login_attempts=3,locked_until=? WHERE id=?",
                java.sql.Timestamp.from(NOW.minusSeconds(1)), user.getId());
        var result = mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(json(user.getEmail(), "password")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900)).andReturn();
        var response = mapper.readTree(result.getResponse().getContentAsString());
        var identity = accessTokens.verify(response.get("accessToken").asString());
        assertThat(identity.userId()).isEqualTo(user.getId());
        String raw = response.get("refreshToken").asString();
        var token = tokens.findByTokenHash(hasher.hash(raw)).orElseThrow();
        assertThat(token.getUserId()).isEqualTo(user.getId());
        assertThat(token.getTokenHash()).isNotEqualTo(raw);
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(tokens.findByUserId(user.getId())).hasSize(1);
        User updated = users.findById(user.getId()).orElseThrow();
        assertThat(updated.getFailedLoginAttempts()).isZero();
        assertThat(updated.getLockedUntil()).isNull();
        assertThat(updated.getLastLoginAt()).isEqualTo(NOW);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("password", user.getPasswordHash());
        assertThat(new LoginRequest(user.getEmail(), "password").toString()).doesNotContain(user.getEmail(), "password");
    }

    @Test
    void fifthFailureCommitsLockButRemainsUniform401AndCorrectPasswordSees423() throws Exception {
        User user = user("password");
        for (int i = 1; i <= 5; i++) {
            invalid(user.getEmail(), "wrong");
            assertThat(users.findById(user.getId()).orElseThrow().getFailedLoginAttempts()).isEqualTo(i);
        }
        User locked = users.findById(user.getId()).orElseThrow();
        assertThat(locked.getLockedUntil()).isEqualTo(NOW.plusSeconds(300));
        assertThat(locked.getStatus()).isEqualTo(UserStatus.ACTIVE);
        invalid(user.getEmail(), "wrong");
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json(user.getEmail(), "password")))
                .andExpect(status().isLocked()).andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_LOCKED"));
        assertThat(users.findById(user.getId()).orElseThrow().getFailedLoginAttempts()).isEqualTo(5);
        assertThat(users.findById(user.getId()).orElseThrow().getLockedUntil()).isEqualTo(locked.getLockedUntil());
        assertThat(tokens.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void cooldownEndsAtExactBoundaryAndOnlySuccessfulLoginResetsCounter() throws Exception {
        User user = user("password");
        for (int i = 0; i < 5; i++) invalid(user.getEmail(), "wrong");
        clock.set(NOW.plusSeconds(299));
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json(user.getEmail(), "password")))
                .andExpect(status().isLocked());
        clock.set(NOW.plusSeconds(300));
        invalid(user.getEmail(), "wrong");
        assertThat(users.findById(user.getId()).orElseThrow().getFailedLoginAttempts()).isEqualTo(6);
        assertThat(users.findById(user.getId()).orElseThrow().getLockedUntil()).isEqualTo(NOW.plusSeconds(600));
        clock.set(NOW.plusSeconds(600));
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json(user.getEmail(), "password")))
                .andExpect(status().isOk());
        assertThat(users.findById(user.getId()).orElseThrow().getFailedLoginAttempts()).isZero();
    }

    @Test
    void nonexistentWrongPasswordGoogleAndManualLockedWrongHaveSamePublicError() throws Exception {
        User user = user("password");
        User manual = user("password");
        jdbc.update("UPDATE users SET status='LOCKED' WHERE id=?", manual.getId());
        User google = user("password");
        jdbc.update("UPDATE users SET auth_provider='GOOGLE',password_hash=NULL,provider_user_id=? WHERE id=?",
                UUID.randomUUID().toString(), google.getId());
        String expected = comparableError(invalid(user.getEmail(), "wrong"));
        for (String email : new String[] {UUID.randomUUID() + "@example.test", google.getEmail(), manual.getEmail()}) {
            assertThat(comparableError(invalid(email, "wrong"))).isEqualTo(expected);
        }
        assertThat(comparableError(invalid(user.getEmail(), "ệ".repeat(30)))).isEqualTo(expected);
        assertThat(comparableError(invalid(UUID.randomUUID() + "@example.test", "ệ".repeat(30)))).isEqualTo(expected);
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json(manual.getEmail(), "password")))
                .andExpect(status().isLocked());
        assertThat(users.findById(manual.getId()).orElseThrow().getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(users.findById(google.getId()).orElseThrow().getFailedLoginAttempts()).isZero();
    }

    @Test
    void concurrentFiveFailuresCannotLoseCountersOrCreateSessions() throws Exception {
        User user = user("password");
        CyclicBarrier start = new CyclicBarrier(5);
        try (var executor = Executors.newFixedThreadPool(5)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < 5; i++) futures.add(executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(json(user.getEmail(), "wrong"))).andReturn().getResponse().getStatus();
            }));
            for (var future : futures) assertThat(future.get(45, TimeUnit.SECONDS)).isEqualTo(401);
        }
        User after = users.findById(user.getId()).orElseThrow();
        assertThat(after.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(after.getLockedUntil()).isEqualTo(NOW.plusSeconds(300));
        assertThat(tokens.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void validatesRequestBeforeAttemptAndDoesNotTrustClientRole() throws Exception {
        User user = user("password");
        for (String body : new String[] {"{}", "{", json("invalid", "password"), json(user.getEmail(), ""),
                json(user.getEmail(), "x".repeat(101))}) {
            mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        assertThat(users.findById(user.getId()).orElseThrow().getFailedLoginAttempts()).isZero();
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(json(user.getEmail(), "password").replace("}", ",\"role\":\"ADMIN\"}")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void verifiesShortExistingPasswordsLongArgonAndCanonicalAdminRole() throws Exception {
        for (String raw : new String[] {"x", "x".repeat(100), "ệ".repeat(30)}) {
            User user = user(raw);
            jdbc.update("UPDATE users SET role='ADMIN' WHERE id=?", user.getId());
            var result = mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                            .content(json(user.getEmail(), raw)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("ADMIN")).andReturn();
            var response = mapper.readTree(result.getResponse().getContentAsString());
            assertThat(accessTokens.verify(response.get("accessToken").asString()).role()).isEqualTo(UserRole.ADMIN);
        }
    }

    private User user(String password) { return users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", password, "Người học", passwords)); }
    private String json(String email, String password) { return mapper.writeValueAsString(Map.of("email", email, "password", password)); }
    private String invalid(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json(email, password)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"))
                .andReturn().getResponse().getContentAsString();
    }
    private String comparableError(String json) {
        var node = mapper.readTree(json);
        return node.get("status") + "|" + node.get("code") + "|" + node.get("message") + "|" + node.get("path") + "|" + node.get("details");
    }

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> current = new AtomicReference<>(NOW);
        void set(Instant value) { current.set(value); }
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return current.get(); }
    }
    @TestConfiguration(proxyBeanMethods = false)
    static class TimeConfiguration {
        @Bean @Primary MutableClock loginTestClock() { return new MutableClock(); }
    }
}
