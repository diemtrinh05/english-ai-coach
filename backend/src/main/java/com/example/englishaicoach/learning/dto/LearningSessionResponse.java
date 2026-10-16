package com.example.englishaicoach.learning.dto;

import com.example.englishaicoach.learning.LearningSessionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LearningSessionResponse(
        UUID sessionId,
        LearningSessionType sessionType,
        Instant startedAt,
        Instant endedAt,
        int wordsStudiedCount,
        BigDecimal accuracyPercent) {
}
