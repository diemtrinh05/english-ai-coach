package com.example.englishaicoach.auth.dto;

public record RefreshResponse(String accessToken, long expiresIn, String tokenType, String refreshToken) {
    @Override public String toString() { return "RefreshResponse[tokens=REDACTED]"; }
}
