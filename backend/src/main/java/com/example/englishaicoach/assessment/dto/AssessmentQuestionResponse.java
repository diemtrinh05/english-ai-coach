package com.example.englishaicoach.assessment.dto;

import java.util.List;
import java.util.UUID;

/** DTO công khai chỉ chứa snapshot câu hỏi, không có correctAnswer. */
public record AssessmentQuestionResponse(UUID questionId, String questionText,
        List<String> options, String cefrLevel) {
    public AssessmentQuestionResponse { options = List.copyOf(options); }
}
