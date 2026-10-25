package com.example.englishaicoach.assessment.dto;

import java.util.UUID;

public record SubmitAssessmentAnswerRequest(UUID questionId, String answer, int responseTimeMs, UUID eventId) { }
