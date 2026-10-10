package com.example.englishaicoach.auth;

import com.example.englishaicoach.common.exception.ApiException;
import org.springframework.http.HttpStatus;

// Chỉ lỗi từ chối đăng nhập dự kiến được commit bộ đếm; lỗi hệ thống vẫn rollback.
final class LoginRejectedException extends ApiException {
    private static final long serialVersionUID = 1L;

    LoginRejectedException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }
}
