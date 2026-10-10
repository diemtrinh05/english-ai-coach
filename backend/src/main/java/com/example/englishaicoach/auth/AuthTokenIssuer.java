package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.config.JwtProperties;
import com.example.englishaicoach.config.RefreshTokenProperties;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenIssuer {
    private final RefreshTokenRepository refreshTokens;
    private final RefreshTokenHasher hasher;
    private final JwtAccessTokenService accessTokens;
    private final JwtProperties jwt;
    private final RefreshTokenProperties refreshPolicy;
    private final SecureRandom random = new SecureRandom();

    public AuthTokenIssuer(RefreshTokenRepository refreshTokens, RefreshTokenHasher hasher,
            JwtAccessTokenService accessTokens, JwtProperties jwt, RefreshTokenProperties refreshPolicy) {
        this.refreshTokens = refreshTokens;
        this.hasher = hasher;
        this.accessTokens = accessTokens;
        this.jwt = jwt;
        this.refreshPolicy = refreshPolicy;
    }

    // Caller giữ transaction để cấp token và mutation tài khoản commit cùng nhau.
    public AuthResponse issue(UUID userId, String email, String fullName, UserRole role, UserStatus status, Instant now) {
        Duration lifetime = refreshPolicy.refreshTokenExpiration();
        if (lifetime == null || lifetime.compareTo(jwt.accessTokenExpiration()) <= 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCodes.INTERNAL_ERROR,
                    "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        }
        String accessToken = accessTokens.issue(userId, role);
        byte[] entropy = new byte[32];
        random.nextBytes(entropy);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        refreshTokens.saveAndFlush(RefreshToken.forRawToken(userId, refreshToken,
                now.plus(lifetime), null, hasher));
        return new AuthResponse(accessToken, jwt.accessTokenExpiration().toSeconds(), "Bearer",
                new AuthUserSummary(userId, email, fullName, role, status),
                refreshToken);
    }
}
