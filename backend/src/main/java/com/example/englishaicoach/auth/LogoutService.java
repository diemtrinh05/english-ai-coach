package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.RefreshRequest;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService {
    private final RefreshTokenRepository tokens;
    private final RefreshTokenHasher hasher;
    private final Clock clock;

    public LogoutService(RefreshTokenRepository tokens, RefreshTokenHasher hasher, Clock clock) {
        this.tokens = tokens;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Transactional
    public void logout(UUID userId, RefreshRequest request) {
        // Cùng khóa token với refresh để serialize revoke và rotation trên PostgreSQL.
        RefreshToken token = tokens.findForRefresh(hasher.hash(request.refreshToken()))
                .orElseThrow(LogoutService::invalid);
        if (!token.getUserId().equals(userId)) {
            throw invalid();
        }
        // Token expired vẫn được revoke; retry không sửa thời điểm revoke ban đầu.
        token.revoke(clock.instant());
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.AUTH_REFRESH_TOKEN_INVALID,
                "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
    }
}
