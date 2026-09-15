package com.example.englishaicoach.common.idempotency;

import com.example.englishaicoach.common.exception.IdempotencyKeyReuseException;
import com.example.englishaicoach.config.IdempotencyProperties;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final RequestHashService requestHashService;
    private final ObjectMapper objectMapper;
    private final IdempotencyProperties properties;

    public IdempotencyService(
            IdempotencyRepository repository,
            RequestHashService requestHashService,
            ObjectMapper objectMapper,
            IdempotencyProperties properties) {
        this.repository = repository;
        this.requestHashService = requestHashService;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public <T> IdempotencyResult<T> execute(
            UUID userId,
            UUID eventId,
            IdempotencyRequest request,
            Class<T> responseType,
            Supplier<IdempotencyResult<T>> operation) {
        Objects.requireNonNull(userId, "userId không được null");
        Objects.requireNonNull(eventId, "eventId không được null");
        Objects.requireNonNull(request, "request không được null");
        Objects.requireNonNull(responseType, "responseType không được null");
        Objects.requireNonNull(operation, "operation không được null");

        String requestHash = requestHashService.sha256Canonical(request);
        var existing = repository.findByEventId(eventId);
        if (existing.isPresent()) {
            return replay(existing.get(), userId, request.endpoint(), requestHash, responseType);
        }

        boolean inserted = repository.claim(eventId, userId, request.endpoint(), requestHash);
        if (!inserted) {
            IdempotencyRecord concurrentRecord = repository.findByEventId(eventId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy idempotency claim sau xung đột cho eventId " + eventId));
            return replay(concurrentRecord, userId, request.endpoint(), requestHash, responseType);
        }

        IdempotencyResult<T> result = Objects.requireNonNull(
                operation.get(), "operation không được trả về null");
        repository.storeResponse(
                eventId,
                result.status(),
                objectMapper.writeValueAsString(result.body()));
        return result;
    }

    @Transactional
    public int deleteExpired(Instant referenceTime) {
        Objects.requireNonNull(referenceTime, "referenceTime không được null");
        return repository.deleteCreatedBefore(referenceTime.minus(properties.retention()));
    }

    private <T> IdempotencyResult<T> replay(
            IdempotencyRecord record,
            UUID userId,
            String endpoint,
            String requestHash,
            Class<T> responseType) {
        if (!record.userId().equals(userId)
                || !record.endpoint().equals(endpoint)
                || !record.requestHash().equals(requestHash)) {
            throw new IdempotencyKeyReuseException();
        }
        if (record.responseStatus() == null || record.responseSnapshot() == null) {
            throw new IllegalStateException("Idempotency response chưa hoàn tất cho eventId " + record.eventId());
        }
        T response = objectMapper.readValue(record.responseSnapshot(), responseType);
        return new IdempotencyResult<>(record.responseStatus(), response);
    }
}
