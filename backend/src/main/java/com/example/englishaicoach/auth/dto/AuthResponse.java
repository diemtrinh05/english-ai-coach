package com.example.englishaicoach.auth.dto;

public record AuthResponse(String accessToken, long expiresIn, String tokenType,
        AuthUserSummary user, String refreshToken) {
    @Override
    public String toString() {
        return "AuthResponse[credentials=REDACTED]";
    }
}
