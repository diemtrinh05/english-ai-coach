package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.englishaicoach.config.JwtProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

class JwtAccessTokenServiceTests {
    private static final Instant NOW = Instant.parse("2026-10-09T00:00:00Z");
    private static final UUID USER = UUID.randomUUID();
    private final byte[] key = randomKey();
    private final JwtProperties properties = properties(key);
    private final JwtAccessTokenService tokens = service(NOW);

    @Test
    void issuesOnlyCanonicalIdentityRoleAndTimeClaimsForBothRoles() throws Exception {
        for (UserRole role : UserRole.values()) {
            String token = tokens.issue(USER, role);
            JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();
            assertThat(claims.getClaims().keySet()).containsExactlyInAnyOrder(
                    "sub", "user_id", "role", "iat", "exp");
            assertThat(claims.getSubject()).isEqualTo(USER.toString());
            assertThat(claims.getStringClaim("user_id")).isEqualTo(USER.toString());
            assertThat(claims.getExpirationTime().toInstant()).isEqualTo(NOW.plusSeconds(900));
            assertThat(tokens.verify(token)).isEqualTo(new AccessTokenIdentity(USER, role));
        }
    }

    @Test
    void expiryIsEnforcedExactlyWithoutClockSkew() {
        String token = tokens.issue(USER, UserRole.USER);
        assertThat(service(NOW.plusSeconds(899)).verify(token).userId()).isEqualTo(USER);
        assertThatThrownBy(() -> service(NOW.plusSeconds(900)).verify(token))
                .isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> service(NOW.minusSeconds(1)).verify(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsTamperingWrongKeyMalformedAndUnsignedTokens() throws Exception {
        String valid = tokens.issue(USER, UserRole.USER);
        assertInvalid(valid.substring(0, valid.lastIndexOf('.') + 1) + "AAAA");
        assertInvalid(signed(validClaims(), randomKey(), JWSAlgorithm.HS256));
        assertInvalid("not-a-jwt");
        assertInvalid("x".repeat(8193));
        assertInvalid(new com.nimbusds.jwt.PlainJWT(validClaims()).serialize());
        assertInvalid(signed(validClaims(), key, JWSAlgorithm.HS384));
    }

    @Test
    void rejectsMissingOrInvalidIdentityRoleAndTimeClaims() throws Exception {
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).subject(null).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).claim("user_id", null).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).claim("user_id", UUID.randomUUID().toString()).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).subject("1-1-1-1-1").build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).claim("role", "SUPERADMIN").build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).claim("role", null).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).claim("role", 1).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).issueTime(null).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).expirationTime(null).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).expirationTime(Date.from(NOW)).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).issueTime(Date.from(NOW.plusSeconds(1))).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).expirationTime(Date.from(NOW.plusSeconds(1801))).build()));
        assertInvalid(signed(new JWTClaimsSet.Builder(validClaims()).notBeforeTime(Date.from(NOW.plusSeconds(1))).build()));
    }

    @Test
    void missingKeyFailsClosedAndConfigurationDoesNotRevealSecrets() {
        JwtProperties missing = new JwtProperties(null, Duration.ofMinutes(15));
        JwtAccessTokenService disabled = new JwtAccessTokenService(missing, Clock.fixed(NOW, ZoneOffset.UTC));
        assertThatThrownBy(() -> disabled.issue(USER, UserRole.USER)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> disabled.verify(tokens.issue(USER, UserRole.USER)))
                .isInstanceOf(BadCredentialsException.class);
        assertThat(properties.toString()).contains("REDACTED").doesNotContain(Base64.getEncoder().encodeToString(key));
        byte[] copy = properties.signingKey();
        copy[0] ^= 1;
        assertThat(properties.signingKey()).isEqualTo(key);
    }

    @Test
    void validatesSecretAndConfiguredLifetime() {
        assertThatThrownBy(() -> new JwtProperties("%%%", Duration.ofMinutes(15)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("JWT_SECRET phải là base64 hợp lệ.");
        assertThatThrownBy(() -> properties(new byte[8])).isInstanceOf(IllegalArgumentException.class);
        for (Duration duration : new Duration[] {Duration.ZERO, Duration.ofMinutes(14), Duration.ofMinutes(31)}) {
            assertThatThrownBy(() -> new JwtProperties(null, duration)).isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(new JwtProperties(null, Duration.ofMinutes(30)).accessTokenExpiration()).isEqualTo(Duration.ofMinutes(30));
    }

    private JwtAccessTokenService service(Instant now) {
        return new JwtAccessTokenService(properties, Clock.fixed(now, ZoneOffset.UTC));
    }

    private JWTClaimsSet validClaims() {
        return new JWTClaimsSet.Builder().subject(USER.toString()).claim("user_id", USER.toString())
                .claim("role", "USER").issueTime(Date.from(NOW))
                .expirationTime(Date.from(NOW.plusSeconds(900))).build();
    }

    private void assertInvalid(String token) {
        assertThatThrownBy(() -> tokens.verify(token)).isInstanceOf(BadCredentialsException.class)
                .hasMessage("Access token không hợp lệ hoặc đã hết hạn.").hasNoCause();
    }

    private String signed(JWTClaimsSet claims) throws Exception {
        return signed(claims, key, JWSAlgorithm.HS256);
    }

    private String signed(JWTClaimsSet claims, byte[] signingKey, JWSAlgorithm algorithm) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(algorithm), claims);
        jwt.sign(new MACSigner(signingKey));
        return jwt.serialize();
    }

    private static JwtProperties properties(byte[] signingKey) {
        return new JwtProperties(Base64.getEncoder().encodeToString(signingKey), Duration.ofMinutes(15));
    }

    private static byte[] randomKey() {
        byte[] value = new byte[64];
        new SecureRandom().nextBytes(value);
        return value;
    }
}
