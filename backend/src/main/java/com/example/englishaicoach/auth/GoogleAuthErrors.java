package com.example.englishaicoach.auth;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import org.springframework.http.HttpStatus;

final class GoogleAuthErrors {
    private GoogleAuthErrors() { }

    static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.UNAUTHORIZED,
                "Thông tin đăng nhập Google không hợp lệ hoặc đã hết hạn.");
    }

    static ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ApiErrorCodes.INTERNAL_ERROR,
                "Đăng nhập Google hiện không khả dụng. Vui lòng thử lại sau.");
    }
}
