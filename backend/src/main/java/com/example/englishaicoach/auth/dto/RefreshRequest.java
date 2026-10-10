package com.example.englishaicoach.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank(message = "Refresh token không được để trống.") String refreshToken) {
    @Override public String toString() { return "RefreshRequest[token=REDACTED]"; }
}
