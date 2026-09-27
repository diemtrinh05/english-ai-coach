package com.example.englishaicoach.common.security;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiErrorResponse;
import com.example.englishaicoach.common.logging.RequestCorrelationFilter;
import com.example.englishaicoach.config.RateLimitProperties;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public final class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitProperties properties;
    private final RateLimitGate gate;
    private final Clock clock;
    private final ObjectMapper mapper;
    private final ClientIdentityResolver clientIdentityResolver;

    public RateLimitFilter(RateLimitProperties properties, RateLimitGate gate,
            Clock clock, ObjectMapper mapper, ClientIdentityResolver clientIdentityResolver) {
        this.properties = properties;
        this.gate = gate;
        this.clock = clock;
        this.mapper = mapper;
        this.clientIdentityResolver = clientIdentityResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String route = request.getRequestURI().substring(request.getContextPath().length());
        Integer maximum = "POST".equals(request.getMethod()) ? maximum(route) : null;
        if (!properties.enabled() || maximum == null
                || gate.allow(route, clientIdentityResolver.resolve(request), maximum, properties.window())) {
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(RequestCorrelationFilter.ERROR_CODE_ATTRIBUTE, ApiErrorCodes.RATE_LIMITED);
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), new ApiErrorResponse(clock.instant(), 429,
                ApiErrorCodes.RATE_LIMITED,
                "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau.",
                request.getRequestURI(), List.of()));
    }

    private Integer maximum(String uri) {
        return switch (uri) {
            case "/api/v1/auth/login" -> properties.login();
            case "/api/v1/auth/refresh" -> properties.refresh();
            case "/api/v1/auth/google" -> properties.google();
            case "/api/v1/admin/ai-content/generate" -> properties.adminAiGeneration();
            case "/api/v1/learning/personalized-exercise" -> properties.personalizedExercise();
            default -> null;
        };
    }
}
