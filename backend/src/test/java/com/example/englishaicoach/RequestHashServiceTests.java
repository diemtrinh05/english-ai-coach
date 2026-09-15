package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.example.englishaicoach.common.idempotency.IdempotencyRequest;
import com.example.englishaicoach.common.idempotency.RequestHashService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class RequestHashServiceTests {

    private final RequestHashService service = new RequestHashService(new ObjectMapper());

    @Test
    void producesSameHashForCanonicalEquivalentRequestsAndExcludesEventId() {
        Map<String, Object> firstBody = new LinkedHashMap<>();
        firstBody.put("eventId", UUID.randomUUID());
        firstBody.put("answerQuality", 4);
        firstBody.put("vocabularyId", "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA");

        Map<String, Object> reorderedBody = new LinkedHashMap<>();
        reorderedBody.put("vocabularyId", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        reorderedBody.put("answerQuality", 4.0);
        reorderedBody.put("eventId", UUID.randomUUID());

        IdempotencyRequest first = new IdempotencyRequest(
                "post",
                "/api/v1/learning/attempts",
                "/api/v1/learning/attempts",
                Map.of("locale", List.of("vi-VN"), "mode", List.of("review")),
                firstBody);
        IdempotencyRequest second = new IdempotencyRequest(
                "POST",
                "/api/v1/learning/attempts",
                "/api/v1/learning/attempts",
                Map.of("mode", List.of("review"), "locale", List.of("vi-VN")),
                reorderedBody);

        assertEquals(service.sha256Canonical(first), service.sha256Canonical(second));
        assertEquals(64, service.sha256Canonical(first).length());
    }

    @Test
    void changesHashWhenAnyLogicalRequestPartChanges() {
        IdempotencyRequest baseline = request("POST", "/api/v1/items/{id}", "/api/v1/items/1", 4);

        assertNotEquals(
                service.sha256Canonical(baseline),
                service.sha256Canonical(request("PUT", "/api/v1/items/{id}", "/api/v1/items/1", 4)));
        assertNotEquals(
                service.sha256Canonical(baseline),
                service.sha256Canonical(request("POST", "/api/v1/items/{id}", "/api/v1/items/2", 4)));
        assertNotEquals(
                service.sha256Canonical(baseline),
                service.sha256Canonical(request("POST", "/api/v1/items/{id}", "/api/v1/items/1", 2)));
        assertNotEquals(
                service.sha256Canonical(baseline),
                service.sha256Canonical(new IdempotencyRequest(
                        "POST", "/api/v1/items/{itemId}", "/api/v1/items/1",
                        Map.of("include", List.of("result")),
                        Map.of("answerQuality", 4, "eventId", UUID.randomUUID()))));
        assertNotEquals(
                service.sha256Canonical(baseline),
                service.sha256Canonical(new IdempotencyRequest(
                        "POST", "/api/v1/items/{id}", "/api/v1/items/1",
                        Map.of("include", List.of("summary")),
                        Map.of("answerQuality", 4, "eventId", UUID.randomUUID()))));
    }

    @Test
    void normalizesUuidTextInsidePathAndQuery() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        IdempotencyRequest uppercase = new IdempotencyRequest(
                "POST", "/api/v1/items/{id}", "/api/v1/items/" + id.toString().toUpperCase(),
                Map.of("ownerId", List.of(id.toString().toUpperCase())), Map.of("value", 1));
        IdempotencyRequest lowercase = new IdempotencyRequest(
                "POST", "/api/v1/items/{id}", "/api/v1/items/" + id,
                Map.of("ownerId", List.of(id.toString())), Map.of("value", 1));

        assertEquals(service.sha256Canonical(uppercase), service.sha256Canonical(lowercase));
    }

    private IdempotencyRequest request(String method, String route, String path, int quality) {
        return new IdempotencyRequest(
                method,
                route,
                path,
                Map.of("include", List.of("result")),
                Map.of("answerQuality", quality, "eventId", UUID.randomUUID()));
    }
}
