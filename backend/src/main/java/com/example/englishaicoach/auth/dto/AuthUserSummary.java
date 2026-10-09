package com.example.englishaicoach.auth.dto;

import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.auth.UserStatus;
import java.util.UUID;

public record AuthUserSummary(UUID id, String email, String fullName, UserRole role, UserStatus status) {
}
