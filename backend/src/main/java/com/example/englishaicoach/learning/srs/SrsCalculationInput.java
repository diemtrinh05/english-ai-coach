package com.example.englishaicoach.learning.srs;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Đầu vào giá trị thuần; không nhận entity hoặc trạng thái từ client. */
public record SrsCalculationInput(
        BigDecimal oldEaseFactor,
        int oldIntervalDays,
        int oldRepetitions,
        int answerQuality,
        int responseTimeMs,
        Instant now) {

    public SrsCalculationInput {
        Objects.requireNonNull(oldEaseFactor, "oldEaseFactor");
        Objects.requireNonNull(now, "now");
        if (oldEaseFactor.signum() <= 0 || oldIntervalDays < 0 || oldRepetitions < 0
                || answerQuality < 0 || answerQuality > 5 || responseTimeMs < 0) {
            throw new IllegalArgumentException("Đầu vào SRS không hợp lệ");
        }
    }
}
