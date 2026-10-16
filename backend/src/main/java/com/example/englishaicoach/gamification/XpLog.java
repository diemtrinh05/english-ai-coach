package com.example.englishaicoach.gamification;

import java.time.Instant;
import java.util.UUID;

/** Projection bất biến của ledger, không phải response DTO. */
public record XpLog(UUID id, UUID userId, UUID sourceEventId, int xpAmount, String reason,
                    String referenceType, UUID referenceId, Instant createdAt) {
}
