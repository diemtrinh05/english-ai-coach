package com.example.englishaicoach.core.network;

import androidx.annotation.Nullable;

public final class ApiError {
    public enum Kind {
        VALIDATION, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, CONFLICT,
        RATE_LIMITED, SERVER, OFFLINE, UNKNOWN
    }

    public final int status;
    @Nullable public final String code;
    @Nullable public final String message;
    public final Kind kind;

    ApiError(int status, @Nullable String code, @Nullable String message, Kind kind) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.kind = kind;
    }

    public boolean isConcurrentUpdate() { return "CONCURRENT_UPDATE".equals(code); }
    public boolean isIdempotencyKeyReuse() { return "IDEMPOTENCY_KEY_REUSE".equals(code); }
}
