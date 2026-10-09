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
@TestPropertySource(properties = {"app.jwt.secret=", "app.jwt.refresh-token-expiration=7d"})
class RegistrationRollbackIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private RefreshTokenRepository tokens;

    @Test
    void failedAccessIssuanceRollsBackTheAlreadyInsertedUserAndCreatesNoRefreshToken() throws Exception {
        String email = java.util.UUID.randomUUID() + "@example.test";
        long usersBefore = users.count();
        long tokensBefore = tokens.count();
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content("""
                {"email":"%s","password":"password","fullName":"Người học"}
                """.formatted(email)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        assertThat(users.findByEmail(email)).isEmpty();
        assertThat(users.count()).isEqualTo(usersBefore);
        assertThat(tokens.count()).isEqualTo(tokensBefore);
    }
}
