package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    private final LoginService login;

    public LoginController(LoginService login) { this.login = login; }

    @PostMapping("/api/v1/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return login.login(request); }
}
