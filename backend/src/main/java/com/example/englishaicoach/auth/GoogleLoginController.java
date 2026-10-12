package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.GoogleLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleLoginController {
    private final GoogleLoginService login;

    public GoogleLoginController(GoogleLoginService login) { this.login = login; }

    @PostMapping("/api/v1/auth/google")
    public AuthResponse login(@Valid @RequestBody GoogleLoginRequest request) {
        return login.login(request);
    }
}
