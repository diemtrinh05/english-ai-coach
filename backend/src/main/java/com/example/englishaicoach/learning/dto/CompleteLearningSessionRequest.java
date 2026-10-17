package com.example.englishaicoach.learning.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CompleteLearningSessionRequest(@NotNull UUID eventId) { }
