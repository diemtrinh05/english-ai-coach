package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.registration.minimum-length=12", "app.jwt.refresh-token-expiration="})
class RegistrationPolicyIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository tokens;

    @Test
    void configuredMinimumIsEnforcedAndMissingRefreshLifetimeMakesNoPartialWrites() throws Exception {
        long usersBefore = users.count();
        long tokensBefore = tokens.count();
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body("password")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body("long-password")))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        assertThat(users.count()).isEqualTo(usersBefore);
        assertThat(tokens.count()).isEqualTo(tokensBefore);
    }

    private static String body(String password) {
        return """
                {"email":"%s@example.test","password":"%s","fullName":"Người học"}
                """.formatted(java.util.UUID.randomUUID(), password);
    }
}
