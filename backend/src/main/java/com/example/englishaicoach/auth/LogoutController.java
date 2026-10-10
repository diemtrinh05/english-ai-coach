package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.RefreshRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    private final LogoutService logout;

    public LogoutController(LogoutService logout) {
        this.logout = logout;
    }

    @PostMapping("/api/v1/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal AccessTokenIdentity identity,
            @Valid @RequestBody RefreshRequest request) {
        logout.logout(identity.userId(), request);
    }
}
