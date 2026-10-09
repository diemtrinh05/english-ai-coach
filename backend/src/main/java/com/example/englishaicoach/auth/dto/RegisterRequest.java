package com.example.englishaicoach.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email không được để trống.")
        @Email(message = "Email không hợp lệ.")
        @Size(max = 255, message = "Email không được vượt quá 255 ký tự.") String email,
        @NotBlank(message = "Mật khẩu không được để trống.")
        @Size(min = 8, max = 100, message = "Mật khẩu phải có 8 đến 100 ký tự.") String password,
        @NotBlank(message = "Họ tên không được để trống.")
        @Size(min = 1, max = 100, message = "Họ tên phải có 1 đến 100 ký tự.") String fullName) {
    @Override
    public String toString() {
        return "RegisterRequest[credentials=REDACTED]";
    }
}
