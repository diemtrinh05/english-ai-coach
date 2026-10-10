package com.example.englishaicoach.auth;

import com.example.englishaicoach.common.persistence.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends UuidEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @CreationTimestamp(source = SourceType.VM)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    protected RefreshToken() {
    }

    public static RefreshToken forRawToken(UUID userId, String rawToken, Instant expiresAt,
            String deviceInfo, RefreshTokenHasher hasher) {
        RefreshToken token = new RefreshToken();
        token.userId = java.util.Objects.requireNonNull(userId, "userId");
        token.tokenHash = hasher.hash(rawToken);
        token.expiresAt = java.util.Objects.requireNonNull(expiresAt, "expiresAt");
        token.deviceInfo = deviceInfo;
        return token;
    }

    public void consume(Instant now) {
        revokedAt = now;
        lastUsedAt = now;
    }

    public void revoke(Instant now) {
        if (revokedAt == null) {
            revokedAt = java.util.Objects.requireNonNull(now, "now");
        }
    }

    public UUID getUserId() {
        return userId;
    }

    String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }
}
