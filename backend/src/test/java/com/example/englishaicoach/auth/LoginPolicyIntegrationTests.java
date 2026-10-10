package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.config.LoginProtectionProperties;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Import(LoginIntegrationTests.TimeConfiguration.class)
@TestPropertySource(properties = {"app.login-protection.failed-attempts-threshold=2", "app.login-protection.lock-duration=20s"})
class LoginPolicyIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordHashService passwords;

    @Test
    void configuredThresholdAndDurationAreAppliedAndUnsafeConfigurationRejected() throws Exception {
        User user = users.saveAndFlush(User.local(UUID.randomUUID() + "@example.test", "password", "Người học", passwords));
        String json = "{\"email\":\"" + user.getEmail() + "\",\"password\":\"wrong\"}";
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json)).andExpect(status().isUnauthorized());
        assertThat(users.findById(user.getId()).orElseThrow().getLockedUntil()).isNull();
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json)).andExpect(status().isUnauthorized());
        assertThat(users.findById(user.getId()).orElseThrow().getLockedUntil()).isEqualTo(LoginIntegrationTests.NOW.plusSeconds(20));
        assertThatThrownBy(() -> new LoginProtectionProperties(0, Duration.ofMinutes(5))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LoginProtectionProperties(5, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }
}
