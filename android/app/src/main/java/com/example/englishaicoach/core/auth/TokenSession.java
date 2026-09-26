package com.example.englishaicoach.core.auth;

import java.util.Objects;
import java.util.UUID;

public final class TokenSession {
    private final String sessionId;
    private final String accessToken;
    private final String refreshToken;

    public TokenSession(String sessionId, String accessToken, String refreshToken) {
        this.sessionId = requireNonBlank(sessionId);
        this.accessToken = requireNonBlank(accessToken);
        this.refreshToken = requireNonBlank(refreshToken);
    }

    public static TokenSession start(String accessToken, String refreshToken) {
        return new TokenSession(UUID.randomUUID().toString(), accessToken, refreshToken);
    }

    public TokenSession withAccessToken(String newAccessToken) {
        return new TokenSession(sessionId, newAccessToken, refreshToken);
    }

    public String sessionId() { return sessionId; }
    public String accessToken() { return accessToken; }
    public String refreshToken() { return refreshToken; }

    private static String requireNonBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Token session value is required");
        }
        return value;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof TokenSession)) return false;
        TokenSession that = (TokenSession) other;
        return sessionId.equals(that.sessionId)
                && accessToken.equals(that.accessToken)
                && refreshToken.equals(that.refreshToken);
    }

    @Override public int hashCode() {
        return Objects.hash(sessionId, accessToken, refreshToken);
    }
}
