package com.example.englishaicoach.common.exception;

public final class ApiErrorCodes {

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_ACCOUNT_LOCKED";
    public static final String AUTH_REFRESH_TOKEN_INVALID = "AUTH_REFRESH_TOKEN_INVALID";
    public static final String AUTH_REFRESH_TOKEN_EXPIRED = "AUTH_REFRESH_TOKEN_EXPIRED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String RATE_LIMITED = "RATE_LIMITED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String CONCURRENT_UPDATE = "CONCURRENT_UPDATE";
    public static final String IDEMPOTENCY_KEY_REUSE = "IDEMPOTENCY_KEY_REUSE";

    private ApiErrorCodes() {
    }
}
