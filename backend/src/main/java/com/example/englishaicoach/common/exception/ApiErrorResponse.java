package com.example.englishaicoach.common.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<Map<String, Object>> details) {

    public ApiErrorResponse {
        details = details == null ? List.of() : List.copyOf(details);
    }
}
