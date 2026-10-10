package com.example.englishaicoach.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.login-protection")
public record LoginProtectionProperties(int failedAttemptsThreshold, Duration lockDuration) {
    public LoginProtectionProperties {
        if (failedAttemptsThreshold < 1 || lockDuration == null || lockDuration.isNegative() || lockDuration.isZero()) {
            throw new IllegalArgumentException("Ngưỡng sai và thời gian khóa đăng nhập phải dương.");
        }
    }
}
