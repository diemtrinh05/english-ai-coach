package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.RegisterRequest;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.config.JwtProperties;
import com.example.englishaicoach.config.RefreshTokenProperties;
import com.example.englishaicoach.config.RegistrationProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final RegistrationRepository users;
    private final PasswordHashService passwords;
    private final JwtProperties jwt;
    private final RefreshTokenProperties refreshPolicy;
    private final RegistrationProperties registration;
    private final Clock clock;
    private final AuthTokenIssuer tokens;

    public RegistrationService(RegistrationRepository users,
            PasswordHashService passwords,
            JwtProperties jwt, RefreshTokenProperties refreshPolicy, RegistrationProperties registration,
            Clock clock, AuthTokenIssuer tokens) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
        this.refreshPolicy = refreshPolicy;
        this.registration = registration;
        this.clock = clock;
        this.tokens = tokens;
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
        return tokens.issue(userId, request.email(), request.fullName(), UserRole.USER, UserStatus.ACTIVE, now);
    }
}
