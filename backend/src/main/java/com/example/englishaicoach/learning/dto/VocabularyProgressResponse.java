package com.example.englishaicoach.learning.dto;

import com.example.englishaicoach.learning.LearningStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VocabularyProgressResponse(UUID vocabularyId, LearningStatus status, BigDecimal easeFactor,
        int intervalDays, int repetitions, Instant nextReviewAt, int correctCount, int incorrectCount) {
}
