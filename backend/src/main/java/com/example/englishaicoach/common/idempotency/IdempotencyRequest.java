package com.example.englishaicoach.common.idempotency;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Ngữ cảnh request cần thiết để tạo canonical hash cho một logical operation.
 */
public record IdempotencyRequest(
        String method,
        String routeTemplate,
        String path,
        Map<String, List<String>> query,
        Object body) {

    public IdempotencyRequest {
        method = requireText(method, "method");
        routeTemplate = requireText(routeTemplate, "routeTemplate");
        path = requireText(path, "path");
        query = query == null ? Map.of() : Map.copyOf(query);
    }

    public String endpoint() {
        return routeTemplate;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " không được null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " không được để trống");
        }
        return value;
    }
}
