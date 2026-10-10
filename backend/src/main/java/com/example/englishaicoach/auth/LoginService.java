package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.LoginRequest;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.config.LoginProtectionProperties;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {
    private final UserRepository users;
    private final PasswordHashService passwords;
    private final AuthTokenIssuer tokens;
    private final LoginProtectionProperties protection;
    private final Clock clock;
    private final String dummyHash;
    private final String dummyLongHash;

    public LoginService(UserRepository users, PasswordHashService passwords, AuthTokenIssuer tokens,
            LoginProtectionProperties protection, Clock clock) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
        this.protection = protection;
        this.clock = clock;
        String dummy = UUID.randomUUID().toString();
        dummyHash = passwords.hash(dummy);
        dummyLongHash = passwords.hash(dummy.repeat(3));
    }

    @Transactional(noRollbackFor = LoginRejectedException.class)
    public AuthResponse login(LoginRequest request) {
        // Khóa hàng đến commit để những request đồng thời không làm mất lần sai.
        User user = users.findForLogin(request.email()).orElse(null);
        boolean local = user != null && user.getAuthProvider() == AuthProvider.LOCAL
                && user.getPasswordHash() != null;
        boolean longPassword = request.password().getBytes(StandardCharsets.UTF_8).length > 72;
        boolean supportedHash = local && (!longPassword || user.getPasswordHash().startsWith("$argon2id$"));
        // BCrypt không verify password >72 byte; vẫn chạy dummy hash để tránh fast-path lộ tài khoản.
        String verificationHash = supportedHash ? user.getPasswordHash() : longPassword ? dummyLongHash : dummyHash;
        boolean verified = passwords.matches(request.password(), verificationHash);
        boolean matches = supportedHash && verified;
        Instant now = clock.instant();
        boolean locked = user != null && (user.getStatus() == UserStatus.LOCKED
                || user.getLockedUntil() != null && user.getLockedUntil().isAfter(now));
        if (!local || !matches) {
            if (local && !locked) {
                user.recordFailedLogin(now, protection.failedAttemptsThreshold(), protection.lockDuration());
            }
            // Credentials sai luôn cùng lỗi, kể cả tài khoản đang bị khóa.
            throw new LoginRejectedException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.AUTH_INVALID_CREDENTIALS,
                    "Email hoặc mật khẩu không đúng.");
        }
        if (locked) {
            throw new LoginRejectedException(HttpStatus.LOCKED, ApiErrorCodes.AUTH_ACCOUNT_LOCKED,
                    "Tài khoản đang bị khóa. Vui lòng thử lại sau.");
        }
        user.recordSuccessfulLogin(now);
        return tokens.issue(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getStatus(), now);
    }
}
