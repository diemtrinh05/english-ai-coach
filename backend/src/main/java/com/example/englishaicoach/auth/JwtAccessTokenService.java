package com.example.englishaicoach.auth;

import com.example.englishaicoach.config.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public final class JwtAccessTokenService {
    private final JwtProperties properties;
    private final Clock clock;

    public JwtAccessTokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public String issue(UUID userId, UserRole role) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(role);
        byte[] key = properties.signingKey();
        if (key == null) {
            throw new IllegalStateException("Chưa cấu hình khóa ký access token.");
        }
        Instant issuedAt = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        // SRS dùng user_id; technical spec dùng sub. Hai claim cùng trỏ một user.
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString()).claim("user_id", userId.toString())
                .claim("role", role.name()).issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plus(properties.accessTokenExpiration())))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT).build(), claims);
        try {
            jwt.sign(new MACSigner(key));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Không thể cấp access token.");
        }
    }

    public AccessTokenIdentity verify(String token) {
        try {
            byte[] key = properties.signingKey();
            if (key == null || token == null || token.length() > 8192) {
                throw invalidToken();
            }
            SignedJWT jwt = SignedJWT.parse(token);
            // Chỉ chấp nhận thuật toán cấu hình, không trust alg do client chọn.
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(new MACVerifier(key))) {
                throw invalidToken();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            UUID userId = UUID.fromString(claims.getSubject());
            if (!userId.toString().equals(claims.getSubject())
                    || !claims.getSubject().equals(claims.getStringClaim("user_id"))) {
                throw invalidToken();
            }
            UserRole role = UserRole.valueOf(claims.getStringClaim("role"));
            Date issuedAt = claims.getIssueTime();
            Date expiresAt = claims.getExpirationTime();
            Instant now = clock.instant();
            if (issuedAt == null || expiresAt == null
                    || issuedAt.toInstant().isAfter(now)
                    || !expiresAt.toInstant().isAfter(now)
                    || !expiresAt.after(issuedAt)
                    || java.time.Duration.between(issuedAt.toInstant(), expiresAt.toInstant())
                            .compareTo(java.time.Duration.ofMinutes(30)) > 0
                    || (claims.getNotBeforeTime() != null
                            && claims.getNotBeforeTime().toInstant().isAfter(now))) {
                throw invalidToken();
            }
            return new AccessTokenIdentity(userId, role);
        } catch (ParseException | JOSEException | IllegalArgumentException | NullPointerException exception) {
            // Không gắn token hay parser exception vào lỗi/log để tránh lộ credential.
            throw invalidToken();
        }
    }

    private static BadCredentialsException invalidToken() {
        return new BadCredentialsException("Access token không hợp lệ hoặc đã hết hạn.");
    }
}
