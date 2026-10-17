package com.example.englishaicoach.learning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.englishaicoach.learning.LearningStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record VocabularyProgressResponse(UUID vocabularyId, LearningStatus status, BigDecimal easeFactor,
        int intervalDays, int repetitions, Instant nextReviewAt, int correctCount, int incorrectCount) {
}
