package com.example.englishaicoach.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class AuthRepositoryIntegrationTests extends PostgreSqlIntegrationTestSupport {

    @Autowired
    private UserRepository users;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Autowired
    private PasswordHashService passwords;

    @Autowired
    private RefreshTokenHasher hasher;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID userId;
    private UUID tokenId;

    @AfterEach
    void cleanUp() {
        if (tokenId != null) {
            jdbc.update("DELETE FROM refresh_tokens WHERE id = ?", tokenId);
        }
        if (userId != null) {
            jdbc.update("DELETE FROM users WHERE id = ?", userId);
        }
    }

    @Test
    void repositoriesPersistCanonicalColumnsWithoutPlaintext() {
        String email = "auth-" + UUID.randomUUID() + "@example.com";
        String rawPassword = "test password 123";
        User user = users.saveAndFlush(User.local(email, rawPassword, "Người học", passwords));
        userId = user.getId();

        String rawToken = "opaque-token-" + UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(86_400);
        RefreshToken token = refreshTokens.saveAndFlush(RefreshToken.forRawToken(
                userId, rawToken, expiresAt, "Android", hasher));
        tokenId = token.getId();

        User loadedUser = users.findByEmail(email).orElseThrow();
        RefreshToken loadedToken = refreshTokens.findByTokenHash(hasher.hash(rawToken))
                .orElseThrow();
        assertEquals(userId, loadedUser.getId());
        assertEquals(tokenId, loadedToken.getId());
        assertEquals(userId, loadedToken.getUserId());
        assertEquals(1, refreshTokens.findByUserId(userId).size());
        assertTrue(passwords.matches(rawPassword, loadedUser.getPasswordHash()));
        assertNotEquals(rawPassword, jdbc.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?", String.class, userId));
        assertEquals(hasher.hash(rawToken), jdbc.queryForObject(
                "SELECT token_hash FROM refresh_tokens WHERE id = ?", String.class, tokenId));
        assertNotEquals(rawToken, loadedToken.getTokenHash());
        assertNotNull(loadedUser.getCreatedAt());
        assertNotNull(loadedToken.getCreatedAt());
    }

    @Test
    void googleUserMapsNullablePasswordAndProviderIdentity() {
        userId = UUID.randomUUID();
        String providerId = "google-" + userId;
        Instant now = Instant.now();
        jdbc.update("""
                INSERT INTO users (id, email, password_hash, auth_provider, provider_user_id,
                    full_name, role, status, failed_login_attempts, created_at, updated_at)
                VALUES (?, ?, NULL, 'GOOGLE', ?, 'Người học Google', 'USER', 'ACTIVE', 0, ?, ?)
                """, userId, "google-" + userId + "@example.com", providerId,
                Timestamp.from(now), Timestamp.from(now));

        User loaded = users.findByAuthProviderAndProviderUserId(AuthProvider.GOOGLE, providerId)
                .orElseThrow();
        assertEquals(userId, loaded.getId());
        assertEquals(AuthProvider.GOOGLE, loaded.getAuthProvider());
        assertNull(loaded.getPasswordHash());
        assertEquals(providerId, loaded.getProviderUserId());
    }
}
