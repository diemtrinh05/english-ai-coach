package com.example.englishaicoach.learning.dto;

import com.example.englishaicoach.learning.LearningSessionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartLearningSessionRequest(
        @NotNull LearningSessionType sessionType,
        @NotNull UUID eventId) {
}
