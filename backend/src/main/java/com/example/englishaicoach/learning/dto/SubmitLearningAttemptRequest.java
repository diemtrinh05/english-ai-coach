package com.example.englishaicoach.learning.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.example.englishaicoach.learning.AttemptType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmitLearningAttemptRequest(
        @NotNull(message = "sessionId là bắt buộc") UUID sessionId,
        @NotNull(message = "vocabularyId là bắt buộc") UUID vocabularyId,
        @NotNull(message = "attemptType là bắt buộc") AttemptType attemptType,
        @NotNull(message = "responseTimeMs là bắt buộc")
        @Min(value = 0, message = "responseTimeMs phải lớn hơn hoặc bằng 0") Integer responseTimeMs,
        @NotNull(message = "answerQuality là bắt buộc")
        @Min(value = 0, message = "answerQuality phải lớn hơn hoặc bằng 0")
        @Max(value = 5, message = "answerQuality phải nhỏ hơn hoặc bằng 5") Integer answerQuality,
        @NotNull(message = "eventId là bắt buộc") UUID eventId) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Trường không được hỗ trợ: " + property);
    }
}
