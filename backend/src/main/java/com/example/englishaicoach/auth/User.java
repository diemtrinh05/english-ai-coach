package com.example.englishaicoach.auth;

import com.example.englishaicoach.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected User() {
    }

    public static User local(String email, String rawPassword, String fullName,
            PasswordHashService passwordHashService) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHashService.hash(rawPassword);
        user.authProvider = AuthProvider.LOCAL;
        user.fullName = fullName;
        user.role = UserRole.USER;
        user.status = UserStatus.ACTIVE;
        user.failedLoginAttempts = 0;
        return user;
    }

    public String getEmail() {
        return email;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
