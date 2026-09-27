package com.example.englishaicoach.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.englishaicoach.common.security.ClientIdentityResolver;
import com.example.englishaicoach.common.security.LocalRateLimitGate;
import com.example.englishaicoach.common.security.RateLimitFilter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;
import tools.jackson.databind.ObjectMapper;

class SecurityFoundationTests {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-27T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void corsOnlyReflectsExplicitAdminOrigin() throws Exception {
        CorsFilter filter = new CorsFilter(new CorsPolicyConfiguration()
                .corsConfigurationSource(new CorsPolicyProperties(List.of("https://admin.example.test"))));
        MockHttpServletRequest allowed = preflight("https://admin.example.test");
        MockHttpServletResponse allowedResponse = new MockHttpServletResponse();
        filter.doFilter(allowed, allowedResponse, new MockFilterChain());
        assertThat(allowedResponse.getStatus()).isEqualTo(200);
        assertThat(allowedResponse.getHeader("Access-Control-Allow-Origin"))
                .isEqualTo("https://admin.example.test");

        MockHttpServletRequest denied = preflight("https://other.example.test");
        MockHttpServletResponse deniedResponse = new MockHttpServletResponse();
        filter.doFilter(denied, deniedResponse, new MockFilterChain());
        assertThat(deniedResponse.getStatus()).isEqualTo(403);
        assertThat(deniedResponse.getHeader("Access-Control-Allow-Origin")).isNull();

        assertThatThrownBy(() -> new CorsPolicyProperties(List.of("*")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fiveCanonicalPostRoutesReturnCanonical429AtConfiguredLimit() throws Exception {
        RateLimitProperties properties = new RateLimitProperties(true, Duration.ofMinutes(1),
                1, 1, 1, 1, 1, List.of());
        RateLimitFilter filter = new RateLimitFilter(properties, new LocalRateLimitGate(CLOCK),
                CLOCK, new ObjectMapper(), new ClientIdentityResolver(List.of()));
        for (String route : List.of("/api/v1/auth/login", "/api/v1/auth/refresh",
                "/api/v1/auth/google", "/api/v1/admin/ai-content/generate",
                "/api/v1/learning/personalized-exercise")) {
            MockHttpServletResponse first = invoke(filter, "POST", route, "192.0.2.1");
            MockHttpServletResponse second = invoke(filter, "POST", route, "192.0.2.1");
            assertThat(first.getStatus()).as(route).isEqualTo(200);
            assertThat(second.getStatus()).as(route).isEqualTo(429);
            assertThat(second.getContentAsString()).contains("\"code\":\"RATE_LIMITED\"");
            assertThat(second.getContentAsString()).contains("\"path\":\"" + route + "\"");
        }
        assertThat(invoke(filter, "POST", "/api/v1/auth/login", "192.0.2.2").getStatus())
                .isEqualTo(200);
        assertThat(invoke(filter, "GET", "/api/v1/auth/login", "192.0.2.1").getStatus())
                .isEqualTo(200);
    }

    @Test
    void enabledPolicyRequiresAllThresholdsAndWindow() {
        assertThatThrownBy(() -> new RateLimitProperties(true, Duration.ofMinutes(1),
                1, 1, 1, null, 1, List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RateLimitProperties(true, Duration.ZERO,
                1, 1, 1, 1, 1, List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void trustedProxySeparatesRealClientsAndIgnoresSpoofedLeftmostHop() throws Exception {
        RateLimitProperties properties = new RateLimitProperties(true, Duration.ofMinutes(1),
                1, 1, 1, 1, 1, List.of("10.0.0.1"));
        RateLimitFilter filter = new RateLimitFilter(properties, new LocalRateLimitGate(CLOCK),
                CLOCK, new ObjectMapper(), new ClientIdentityResolver(properties.trustedProxies()));
        String route = "/api/v1/auth/login";
        assertThat(invoke(filter, "POST", route, "10.0.0.1", "203.0.113.250, 198.51.100.1")
                .getStatus()).isEqualTo(200);
        assertThat(invoke(filter, "POST", route, "10.0.0.1", "203.0.113.251, 198.51.100.1")
                .getStatus()).isEqualTo(429);
        assertThat(invoke(filter, "POST", route, "10.0.0.1", "198.51.100.2")
                .getStatus()).isEqualTo(200);
        assertThat(invoke(filter, "POST", route, "198.51.100.3", "198.51.100.4")
                .getStatus()).isEqualTo(200);
        assertThat(invoke(filter, "POST", route, "198.51.100.3", "198.51.100.5")
                .getStatus()).isEqualTo(429);
    }

    private static MockHttpServletRequest preflight(String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/admin/users");
        request.addHeader("Origin", origin);
        request.addHeader("Access-Control-Request-Method", "GET");
        return request;
    }

    private static MockHttpServletResponse invoke(RateLimitFilter filter, String method,
            String path, String remoteAddress) throws Exception {
        return invoke(filter, method, path, remoteAddress, null);
    }

    private static MockHttpServletResponse invoke(RateLimitFilter filter, String method,
            String path, String remoteAddress, String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteAddress);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
