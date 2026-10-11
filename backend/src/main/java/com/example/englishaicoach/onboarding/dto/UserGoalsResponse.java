package com.example.englishaicoach.onboarding.dto;

import java.util.List;

public record UserGoalsResponse(List<UserGoalResponse> goals) {
    public UserGoalsResponse { goals = List.copyOf(goals); }
}
