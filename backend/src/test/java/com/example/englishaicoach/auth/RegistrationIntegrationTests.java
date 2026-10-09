package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.auth.dto.RegisterRequest;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
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
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Import(RegistrationIntegrationTests.FixedTimeConfiguration.class)
class RegistrationIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final Instant NOW = Instant.parse("2026-10-09T00:00:00Z");
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository refreshTokens;
    @Autowired private RefreshTokenHasher hasher;
    @Autowired private PasswordHashService passwords;
    @Autowired private JwtAccessTokenService accessTokens;
    @Autowired private ObjectMapper mapper;

    @DynamicPropertySource
    static void configuration(DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String secret = Base64.getEncoder().encodeToString(key);
        registry.add("app.jwt.secret", () -> secret);
        registry.add("app.jwt.refresh-token-expiration", () -> "7d");
    }

    @Test
    void registersPublicUserWithCanonicalResponseHashedCredentialsAndFixedExpiry() throws Exception {
        String email = email();
        String rawPassword = "StrongPassword123!";
        var result = mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                        .content(json(email, rawPassword, "Người học")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.fullName").value("Người học"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900)).andReturn();
        var response = mapper.readTree(result.getResponse().getContentAsString());
        User user = users.findByEmail(email).orElseThrow();
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(passwords.matches(rawPassword, user.getPasswordHash())).isTrue();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(rawPassword, user.getPasswordHash());
        assertThat(accessTokens.verify(response.get("accessToken").asString()).userId()).isEqualTo(user.getId());
        String rawRefresh = response.get("refreshToken").asString();
        assertThat(Base64.getUrlDecoder().decode(rawRefresh)).hasSize(32);
        RefreshToken token = refreshTokens.findByTokenHash(hasher.hash(rawRefresh)).orElseThrow();
        assertThat(token.getUserId()).isEqualTo(user.getId());
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(token.getRevokedAt()).isNull();
        assertThat(token.getTokenHash()).isNotEqualTo(rawRefresh);
        assertThat(new RegisterRequest(email, rawPassword, "name").toString()).doesNotContain(rawPassword);
    }

    @Test
    void rejectsInvalidClientFieldsWithoutDatabaseWrites() throws Exception {
        long before = users.count();
        for (String content : new String[] {"{}", "{", json("invalid", "password", "name"),
                json(email(), "short", "name"), json(email(), "x".repeat(101), "name"),
                json(email(), "password", ""), json(email(), "password", "x".repeat(101))}) {
            mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(content))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        assertThat(users.count()).isEqualTo(before);
    }

    @Test
    void clientSuppliedRoleCannotEscalateDefaultUserRole() throws Exception {
        String body = json(email(), "password", "name").replace("}", ",\"role\":\"ADMIN\"}");
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"));
    }

    @Test
    void duplicateEmailAlwaysReturnsConflictWithoutSecondSession() throws Exception {
        String email = email();
        String body = json(email, "password", "name");
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
        }
        User user = users.findByEmail(email).orElseThrow();
        assertThat(refreshTokens.findByUserId(user.getId())).hasSize(1);
    }

    @Test
    void simultaneousDuplicateRegistrationCreatesOneUserAndOneRefreshToken() throws Exception {
        String email = email();
        String body = json(email, "password", "name");
        CyclicBarrier start = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Integer> request = () -> {
                start.await(10, TimeUnit.SECONDS);
                return mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                        .andReturn().getResponse().getStatus();
            };
            var first = executor.submit(request);
            var second = executor.submit(request);
            assertThat(java.util.List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE email=?", Long.class, email)).isEqualTo(1);
        assertThat(refreshTokens.findByUserId(users.findByEmail(email).orElseThrow().getId())).hasSize(1);
    }

    @Test
    void acceptsAllContractPasswordBoundariesIncludingMoreThan72Utf8Bytes() throws Exception {
        for (String raw : new String[] {"password", "x".repeat(72), "x".repeat(100), "ệ".repeat(30)}) {
            String email = email();
            mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                            .content(json(email, raw, "name"))).andExpect(status().isCreated());
            User user = users.findByEmail(email).orElseThrow();
            assertThat(passwords.matches(raw, user.getPasswordHash())).isTrue();
            assertThat(passwords.matches(raw + "z", user.getPasswordHash())).isFalse();
        }
    }

    private String json(String email, String password, String fullName) {
        return mapper.writeValueAsString(Map.of("email", email, "password", password, "fullName", fullName));
    }

    private static String email() { return UUID.randomUUID() + "@example.test"; }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedTimeConfiguration {
        @Bean @Primary Clock registrationTestClock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    }
}
