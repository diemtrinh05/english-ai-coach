package com.example.englishaicoach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.security.cors.allowed-origins=https://admin.example.test",
        "app.security.rate-limit.enabled=true",
        "app.security.rate-limit.window=1m",
        "app.security.rate-limit.login=1",
        "app.security.rate-limit.refresh=1",
        "app.security.rate-limit.google=1",
        "app.security.rate-limit.admin-ai-generation=1",
        "app.security.rate-limit.personalized-exercise=1",
        "app.security.rate-limit.trusted-proxies=10.0.0.1"
})
class SecurityFilterChainIntegrationTests extends PostgreSqlIntegrationTestSupport {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void springSecurityAppliesCorsAndHeadersWithoutPrematureAuthentication() throws Exception {
        assertThat(applicationContext.getBeanNamesForType(UserDetailsService.class)).isEmpty();
        mockMvc.perform(options("/api/v1/admin/users")
                        .header("Origin", "https://admin.example.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://admin.example.test"));
        mockMvc.perform(options("/api/v1/admin/users")
                        .header("Origin", "https://evil.example.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
        mockMvc.perform(get("/actuator/health").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    @Test
    void rateLimitUsesTrustedProxyIdentityAndCanonicalErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> { request.setRemoteAddr("10.0.0.1"); return request; })
                        .header("X-Forwarded-For", "203.0.113.9, 198.51.100.1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> { request.setRemoteAddr("10.0.0.1"); return request; })
                        .header("X-Forwarded-For", "203.0.113.10, 198.51.100.1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(request -> { request.setRemoteAddr("10.0.0.1"); return request; })
                        .header("X-Forwarded-For", "198.51.100.2"))
                .andExpect(status().isBadRequest());
    }
}
