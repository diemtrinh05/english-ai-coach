package com.example.englishaicoach.config;

import java.time.Duration;
import java.util.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public final class JwtProperties {
    private final byte[] signingKey;
    private final Duration accessTokenExpiration;

    public JwtProperties(String secret, Duration accessTokenExpiration) {
        if (accessTokenExpiration == null
                || accessTokenExpiration.compareTo(Duration.ofMinutes(15)) < 0
                || accessTokenExpiration.compareTo(Duration.ofMinutes(30)) > 0) {
            throw new IllegalArgumentException("Access token phải có expiry từ 15 đến 30 phút.");
        }
        this.accessTokenExpiration = accessTokenExpiration;
        if (secret == null || secret.isBlank()) {
            signingKey = null;
            return;
        }
        try {
            signingKey = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT_SECRET phải là base64 hợp lệ.");
        }
        if (signingKey.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET phải chứa tối thiểu 32 byte ngẫu nhiên.");
        }
    }

    public byte[] signingKey() {
        return signingKey == null ? null : signingKey.clone();
    }

    public Duration accessTokenExpiration() {
        return accessTokenExpiration;
    }

    @Override
    public String toString() {
        return "JwtProperties[secret=REDACTED, accessTokenExpiration=" + accessTokenExpiration + "]";
    }
}
