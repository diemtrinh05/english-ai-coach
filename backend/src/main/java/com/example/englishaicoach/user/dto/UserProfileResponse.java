package com.example.englishaicoach.user.dto;

import com.example.englishaicoach.onboarding.dto.CefrLevelResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

public record UserProfileResponse(String fullName, String avatarUrl,
        @JsonInclude(JsonInclude.Include.NON_NULL) CefrLevelResponse currentCefrLevel,
        int dailyLearningMinutes, String timezone) {
}
