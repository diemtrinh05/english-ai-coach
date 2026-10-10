package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.jwt.secret=", "app.jwt.refresh-token-expiration=7d"})
class LoginRollbackIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;
    @Autowired RefreshTokenRepository tokens;
    @Autowired JdbcTemplate jdbc;

    @Test
    void tokenIssuanceFailureRollsBackResetAndLastLoginWithoutCreatingSession() throws Exception {
        User user = users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học", passwords));
        Instant expired = Instant.parse("2020-01-01T00:00:00Z");
        jdbc.update("UPDATE users SET failed_login_attempts=3,locked_until=? WHERE id=?", java.sql.Timestamp.from(expired), user.getId());
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("""
                {"email":"%s","password":"password"}
                """.formatted(user.getEmail()))).andExpect(status().isInternalServerError());
        User after = users.findById(user.getId()).orElseThrow();
        assertThat(after.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(after.getLockedUntil()).isEqualTo(expired);
        assertThat(after.getLastLoginAt()).isNull();
        assertThat(tokens.findByUserId(user.getId())).isEmpty();
    }
}
