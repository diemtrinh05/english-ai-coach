package com.example.englishaicoach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.registration")
public record RegistrationProperties(int minimumLength) {
    public RegistrationProperties {
        if (minimumLength < 8 || minimumLength > 100) {
            throw new IllegalArgumentException("Minimum password length phải thuộc 8..100.");
        }
    }
}
