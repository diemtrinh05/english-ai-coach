package com.example.englishaicoach.assessment.dto;

import com.example.englishaicoach.assessment.AssessmentStatus;
import com.example.englishaicoach.onboarding.dto.CefrLevelResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssessmentResultResponse(UUID assessmentId, AssessmentStatus status,
        @JsonInclude(JsonInclude.Include.ALWAYS) BigDecimal score,
        int totalQuestions, int correctAnswers,
        @JsonInclude(JsonInclude.Include.NON_NULL) CefrLevelResponse cefrLevel,
        Instant createdAt) { }
