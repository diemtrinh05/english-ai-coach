package com.example.englishaicoach.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email không được để trống.")
        @Email(message = "Email không hợp lệ.") String email,
        @NotBlank(message = "Mật khẩu không được để trống.")
        @Size(min = 1, max = 100, message = "Mật khẩu phải có 1 đến 100 ký tự.") String password) {
    @Override
    public String toString() {
        return "LoginRequest[credentials=REDACTED]";
    }
}
