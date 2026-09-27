package com.example.englishaicoach.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Ngưỡng vận hành được cấp riêng cho từng môi trường. */
@ConfigurationProperties("app.security.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        Duration window,
        Integer login,
        Integer refresh,
        Integer google,
        Integer adminAiGeneration,
        Integer personalizedExercise,
        List<String> trustedProxies) {
    public RateLimitProperties {
        trustedProxies = trustedProxies == null ? List.of() : List.copyOf(trustedProxies);
        if (enabled && (window == null || window.isZero() || window.isNegative()
                || invalid(login) || invalid(refresh) || invalid(google)
                || invalid(adminAiGeneration) || invalid(personalizedExercise))) {
            throw new IllegalArgumentException("Rate limit bật phải có window và năm ngưỡng dương");
        }
    }

    private static boolean invalid(Integer value) {
        return value == null || value <= 0;
    }
}
