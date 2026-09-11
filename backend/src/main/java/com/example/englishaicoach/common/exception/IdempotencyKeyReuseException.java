package com.example.englishaicoach.common.exception;

import org.springframework.http.HttpStatus;

public final class IdempotencyKeyReuseException extends ApiException {

    private static final long serialVersionUID = 1L;

    public IdempotencyKeyReuseException() {
        super(
                HttpStatus.CONFLICT,
                ApiErrorCodes.IDEMPOTENCY_KEY_REUSE,
                "eventId đã được sử dụng cho một thao tác khác.");
    }
}
