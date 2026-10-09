package com.example.englishaicoach.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public record RefreshTokenProperties(Duration refreshTokenExpiration) {
    public RefreshTokenProperties {
        if (refreshTokenExpiration != null && refreshTokenExpiration.isNegative()) {
            throw new IllegalArgumentException("Refresh expiry phải là duration dương.");
        }
    }
}
