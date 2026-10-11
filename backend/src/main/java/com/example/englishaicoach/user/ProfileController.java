package com.example.englishaicoach.user;

import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.user.dto.UpdateProfileRequest;
import com.example.englishaicoach.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {
    private final ProfileService profiles;
    public ProfileController(ProfileService profiles) { this.profiles = profiles; }

    @GetMapping("/api/v1/users/me")
    public AuthUserSummary currentUser(@AuthenticationPrincipal AccessTokenIdentity identity) {
        return profiles.currentUser(identity.userId());
    }

    @GetMapping("/api/v1/users/me/profile")
    public UserProfileResponse currentProfile(@AuthenticationPrincipal AccessTokenIdentity identity) {
        return profiles.currentProfile(identity.userId());
    }

    @PutMapping("/api/v1/users/me/profile")
    public UserProfileResponse update(@AuthenticationPrincipal AccessTokenIdentity identity,
            @Valid @RequestBody UpdateProfileRequest request) {
        return profiles.update(identity.userId(), request);
    }
}
