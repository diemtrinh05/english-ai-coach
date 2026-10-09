package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.auth.dto.RegisterRequest;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.config.JwtProperties;
import com.example.englishaicoach.config.RefreshTokenProperties;
import com.example.englishaicoach.config.RegistrationProperties;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final RegistrationRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordHashService passwords;
    private final RefreshTokenHasher hasher;
    private final JwtAccessTokenService accessTokens;
    private final JwtProperties jwt;
    private final RefreshTokenProperties refreshPolicy;
    private final RegistrationProperties registration;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public RegistrationService(RegistrationRepository users, RefreshTokenRepository refreshTokens,
            PasswordHashService passwords, RefreshTokenHasher hasher, JwtAccessTokenService accessTokens,
            JwtProperties jwt, RefreshTokenProperties refreshPolicy, RegistrationProperties registration,
            Clock clock) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwords = passwords;
        this.hasher = hasher;
        this.accessTokens = accessTokens;
        this.jwt = jwt;
        this.refreshPolicy = refreshPolicy;
        this.registration = registration;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.password().length() < registration.minimumLength()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "Mật khẩu chưa đạt độ dài tối thiểu đã cấu hình.");
        }
        Duration lifetime = refreshPolicy.refreshTokenExpiration();
        if (lifetime == null || lifetime.compareTo(jwt.accessTokenExpiration()) <= 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCodes.INTERNAL_ERROR,
                    "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        }
        UUID userId = UUID.randomUUID();
        Instant now = clock.instant();
        if (!users.insertLocalUser(userId, request.email(), passwords.hash(request.password()),
                request.fullName(), now)) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCodes.CONFLICT,
                    "Yêu cầu xung đột với trạng thái hiện tại.");
        }
        String accessToken = accessTokens.issue(userId, UserRole.USER);
        byte[] entropy = new byte[32];
        random.nextBytes(entropy);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        refreshTokens.saveAndFlush(RefreshToken.forRawToken(userId, refreshToken,
                now.plus(lifetime), null, hasher));
        return new AuthResponse(accessToken, jwt.accessTokenExpiration().toSeconds(), "Bearer",
                new AuthUserSummary(userId, request.email(), request.fullName(), UserRole.USER, UserStatus.ACTIVE),
                refreshToken);
    }
}
