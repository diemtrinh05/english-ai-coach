package com.example.englishaicoach.common.idempotency;

/**
 * Status và body được lưu nguyên dạng để lần retry có thể replay cùng response.
 */
public record IdempotencyResult<T>(int status, T body) {

    public IdempotencyResult {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status phải nằm trong khoảng 100..599");
        }
    }
}
