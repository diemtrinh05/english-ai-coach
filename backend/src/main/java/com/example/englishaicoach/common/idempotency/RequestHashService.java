package com.example.englishaicoach.common.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class RequestHashService {

    private static final Pattern UUID_TEXT = Pattern.compile(
            "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final ObjectMapper objectMapper;

    public RequestHashService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sha256Canonical(IdempotencyRequest request) {
        Map<String, Object> canonicalRequest = new LinkedHashMap<>();
        canonicalRequest.put("method", request.method().toUpperCase(Locale.ROOT));
        canonicalRequest.put("routeTemplate", normalizeUuidText(request.routeTemplate()));
        canonicalRequest.put("path", normalizeUuidText(request.path()));
        canonicalRequest.put("query", canonicalize(objectMapper.valueToTree(request.query())));

        JsonNode body = objectMapper.valueToTree(request.body());
        if (body instanceof ObjectNode objectBody) {
            objectBody.remove("eventId");
        }
        canonicalRequest.put("body", canonicalize(body));

        byte[] canonicalBytes = objectMapper.writeValueAsString(canonicalRequest)
                .getBytes(StandardCharsets.UTF_8);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonicalBytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM không hỗ trợ SHA-256", exception);
        }
    }

    private Object canonicalize(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            Map<String, Object> sorted = new TreeMap<>();
            node.properties().forEach(entry -> sorted.put(entry.getKey(), canonicalize(entry.getValue())));
            return sorted;
        }
        if (node.isArray()) {
            List<Object> values = new ArrayList<>();
            node.forEach(value -> values.add(canonicalize(value)));
            return values;
        }
        if (node.isString()) {
            return normalizeUuidText(node.stringValue());
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isIntegralNumber()) {
            return node.bigIntegerValue();
        }
        if (node.isFloatingPointNumber()) {
            return node.decimalValue().stripTrailingZeros();
        }
        return node.toString();
    }

    private String normalizeUuidText(String value) {
        Matcher matcher = UUID_TEXT.matcher(value);
        StringBuilder normalized = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(
                    normalized,
                    Matcher.quoteReplacement(UUID.fromString(matcher.group()).toString()));
        }
        matcher.appendTail(normalized);
        return normalized.toString();
    }
}
