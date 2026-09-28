package com.example.englishaicoach.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthHashingTests {

    private final PasswordHashService passwords = new PasswordHashService();
    private final RefreshTokenHasher tokens = new RefreshTokenHasher();

    @Test
    void localUserStoresBcryptHashAndCanonicalDefaults() {
        User user = User.local("learner@example.com", "correct horse battery staple",
                "Người học", passwords);

        assertEquals(AuthProvider.LOCAL, user.getAuthProvider());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertNotEquals("correct horse battery staple", user.getPasswordHash());
        assertTrue(passwords.matches("correct horse battery staple", user.getPasswordHash()));
        assertFalse(passwords.matches("wrong password", user.getPasswordHash()));
        assertNotEquals(passwords.hash("correct horse battery staple"), user.getPasswordHash());
    }

    @Test
    void refreshTokenStoresOnlyDeterministicDigest() {
        UUID userId = UUID.randomUUID();
        String rawToken = "opaque-refresh-token-value";
        Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
        RefreshToken token = RefreshToken.forRawToken(userId, rawToken, expiry, null, tokens);

        assertEquals(userId, token.getUserId());
        assertEquals(expiry, token.getExpiresAt());
        assertEquals(64, token.getTokenHash().length());
        assertEquals(tokens.hash(rawToken), token.getTokenHash());
        assertNotEquals(rawToken, token.getTokenHash());
        assertNotEquals(tokens.hash("another-token"), token.getTokenHash());
        assertThrows(IllegalArgumentException.class, () -> tokens.hash(" "));
    }
}
