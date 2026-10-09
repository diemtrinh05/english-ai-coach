package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.config.JwtProperties;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AutoConfigureMockMvc
@Import(JwtAuthenticationIntegrationTests.FixtureConfiguration.class)
class JwtAuthenticationIntegrationTests extends PostgreSqlIntegrationTestSupport {
    private static final UUID USER = UUID.randomUUID();
    private static final String SECRET = randomSecret();
    @Autowired private MockMvc mvc;
    @Autowired private JwtAccessTokenService tokens;
    @Autowired private JwtProperties properties;
    @Autowired private Clock clock;

    @DynamicPropertySource
    static void jwtConfiguration(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> SECRET);
    }

    @Test
    void deniesUnauthenticatedProtectedRequestsAndLogoutWithCanonicalError() throws Exception {
        for (String path : new String[] {"/api/v1/users/me", "/api/v1/admin/users", "/api/v1/learning/today"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.path").value(path))
                    .andExpect(jsonPath("$.details").isArray());
        }
        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesIdentityAndDeniesUserAdminAccessWithoutLeakingToken() throws Exception {
        String token = tokens.issue(USER, UserRole.USER);
        var result = mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(USER.toString())).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(token);
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + tokens.issue(USER, UserRole.ADMIN)))
                .andExpect(status().isOk());
        // Request kế tiếp không được kế thừa SecurityContext của request trước.
        mvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidExpiredFutureWrongKeyAndDuplicateAuthorization() throws Exception {
        JwtAccessTokenService expired = new JwtAccessTokenService(properties,
                Clock.offset(clock, Duration.ofMinutes(-16)));
        JwtAccessTokenService future = new JwtAccessTokenService(properties,
                Clock.offset(clock, Duration.ofMinutes(1)));
        JwtAccessTokenService wrongKey = new JwtAccessTokenService(
                new JwtProperties(randomSecret(), Duration.ofMinutes(15)), clock);
        for (String token : new String[] {"invalid", expired.issue(USER, UserRole.USER),
                future.issue(USER, UserRole.USER), wrongKey.issue(USER, UserRole.ADMIN)}) {
            var response = mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andReturn().getResponse();
            assertThat(response.getContentAsString()).doesNotContain(token);
        }
        String header = "Bearer " + tokens.issue(USER, UserRole.USER);
        mvc.perform(get("/api/v1/users/me").header("Authorization", header, header))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Basic ignored"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keepsExactPublicOperationsAvailableAndProtectsOtherMethods() throws Exception {
        mvc.perform(get("/api/v1/cefr-levels")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/topics")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/vocabulary")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/topics/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/vocabulary/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/vocabulary/" + UUID.randomUUID() + "/examples")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/goals")).andExpect(status().isNotFound());
        for (String route : new String[] {"register", "login", "refresh", "google"}) {
            mvc.perform(post("/api/v1/auth/" + route)).andExpect(status().isNotFound());
        }
        mvc.perform(post("/api/v1/vocabulary")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/topics/one/extra")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    private static String randomSecret() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixtureConfiguration {
        @Bean FixtureController jwtFixtureController() { return new FixtureController(); }
    }

    @RestController
    static class FixtureController {
        @GetMapping({"/api/v1/users/me", "/api/v1/admin/users"})
        Map<String, String> identity(java.security.Principal principal) {
            return Map.of("userId", principal.getName());
        }
    }
}
