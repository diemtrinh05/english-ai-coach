package com.example.englishaicoach.common.idempotency;

import java.time.Instant;
import java.util.UUID;

record IdempotencyRecord(
        UUID eventId,
        UUID userId,
        String endpoint,
        String requestHash,
        String responseSnapshot,
        Integer responseStatus,
        Instant createdAt) {
}
