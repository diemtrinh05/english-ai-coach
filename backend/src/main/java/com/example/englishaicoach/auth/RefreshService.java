package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.RefreshRequest;
import com.example.englishaicoach.auth.dto.RefreshResponse;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshService {
    private final RefreshTokenRepository tokens;
    private final RefreshTokenHasher hasher;
    private final UserRepository users;
    private final AuthTokenIssuer issuer;
    private final Clock clock;
    public RefreshService(RefreshTokenRepository tokens, RefreshTokenHasher hasher,
            UserRepository users, AuthTokenIssuer issuer, Clock clock) {
        this.tokens = tokens; this.hasher = hasher; this.users = users; this.issuer = issuer; this.clock = clock;
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshToken token = tokens.findForRefresh(hasher.hash(request.refreshToken())).orElseThrow(RefreshService::invalid);
        if (token.getRevokedAt() != null) throw invalid();
        // Khóa token trước user; login khóa user nhưng không khóa token đã có.
        User user = users.findForRefresh(token.getUserId()).orElseThrow(RefreshService::invalid);
        if (user.getStatus() == UserStatus.LOCKED) throw invalid();
        Instant now = clock.instant();
        if (!now.isBefore(token.getExpiresAt())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.AUTH_REFRESH_TOKEN_EXPIRED,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        // Revoke và token mới commit cùng nhau; lỗi issuer/persistence rollback toàn bộ.
        token.consume(now);
        return issuer.rotate(user.getId(), user.getRole(), token.getExpiresAt(), token.getDeviceInfo());
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.AUTH_REFRESH_TOKEN_INVALID,
                "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
    }
}
