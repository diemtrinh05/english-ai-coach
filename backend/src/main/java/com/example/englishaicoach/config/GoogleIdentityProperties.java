package com.example.englishaicoach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.google-identity")
public record GoogleIdentityProperties(String clientId) {
    @Override
    public String toString() {
        return "GoogleIdentityProperties[configured=" + (clientId != null && !clientId.isBlank()) + "]";
    }
}
