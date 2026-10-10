package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.RefreshRequest;
import com.example.englishaicoach.auth.dto.RefreshResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshController {
    private final RefreshService refresh;
    public RefreshController(RefreshService refresh) { this.refresh = refresh; }
    @PostMapping("/api/v1/auth/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) { return refresh.refresh(request); }
}
