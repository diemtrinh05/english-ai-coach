package com.example.englishaicoach.learning.srs;

import java.math.BigDecimal;
import java.time.Instant;

/** Kết quả tính thuần; state transitions và persistence thuộc task BE-SRS-002 trở đi. */
public record SrsCalculationResult(
        BigDecimal newEaseFactor,
        int newIntervalDays,
        int newRepetitions,
        Instant nextReviewAt,
        boolean isCorrect,
        String algorithmVersion) {
}
