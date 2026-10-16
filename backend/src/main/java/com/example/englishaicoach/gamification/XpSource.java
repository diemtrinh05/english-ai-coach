package com.example.englishaicoach.gamification;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Nguồn backend đã chấp nhận; caller chịu trách nhiệm eligibility và ownership. */
public record XpSource(UUID userId, XpReason reason, UUID sourceEventId, UUID referenceId) {
    public XpSource {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(sourceEventId, "sourceEventId");
        Objects.requireNonNull(referenceId, "referenceId");
        if (reason != XpReason.STREAK_MAINTAINED && !sourceEventId.equals(referenceId)) {
            throw new IllegalArgumentException("Nguồn XP phải trùng entity gây XP.");
        }
    }

    public static XpSource entity(UUID userId, XpReason reason, UUID entityId) {
        if (reason == XpReason.STREAK_MAINTAINED) {
            throw new IllegalArgumentException("Nguồn streak phải dùng ngày đủ điều kiện.");
        }
        return new XpSource(userId, reason, entityId, entityId);
    }

    public static XpSource streak(UUID userId, UUID streakId, LocalDate qualifyingLocalDate) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(qualifyingLocalDate, "qualifyingLocalDate");
        String identity = "gamification-v1:STREAK_MAINTAINED:" + userId + ":" + qualifyingLocalDate;
        UUID sourceEventId = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
        return new XpSource(userId, XpReason.STREAK_MAINTAINED, sourceEventId, streakId);
    }
}
