package com.example.englishaicoach.onboarding.dto;

import java.util.UUID;

public record UserGoalResponse(UUID id, String name, boolean isPrimary) {}
