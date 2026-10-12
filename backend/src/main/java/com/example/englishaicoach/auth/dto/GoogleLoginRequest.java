package com.example.englishaicoach.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Google ID token không được để trống.") String idToken) {
    @Override
    public String toString() {
        return "GoogleLoginRequest[idToken=REDACTED]";
    }
}
