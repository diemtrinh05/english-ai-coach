package com.example.englishaicoach.auth;

import java.security.Principal;
import java.util.UUID;

public record AccessTokenIdentity(UUID userId, UserRole role) implements Principal {
    @Override
    public String getName() {
        return userId.toString();
    }
}
