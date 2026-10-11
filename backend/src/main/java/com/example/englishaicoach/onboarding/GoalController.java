package com.example.englishaicoach.onboarding;

import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.onboarding.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class GoalController {
    private final GoalService goals;
    public GoalController(GoalService goals) { this.goals=goals; }

    @GetMapping("/api/v1/goals")
    public List<GoalResponse> listGoals() { return goals.listGoals(); }

    @GetMapping("/api/v1/users/me/goals")
    public UserGoalsResponse currentGoals(@AuthenticationPrincipal AccessTokenIdentity identity) {
        return goals.currentGoals(identity.userId());
    }

    @PutMapping("/api/v1/users/me/goals")
    public UserGoalsResponse replace(@AuthenticationPrincipal AccessTokenIdentity identity,@Valid @RequestBody SetUserGoalsRequest request) {
        return goals.replace(identity.userId(),request);
    }
}
