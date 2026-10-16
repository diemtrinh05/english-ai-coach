package com.example.englishaicoach.learning.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LearningAttemptResponse(UUID attemptId, boolean isCorrect, int answerQuality,
        SrsResult srs, VocabularyProgressResponse progress) {
    public record SrsResult(BigDecimal oldEaseFactor, BigDecimal newEaseFactor, int oldIntervalDays,
            int newIntervalDays, int oldRepetitions, int newRepetitions, Instant nextReviewAt,
            String algorithmVersion) {
    }
}
