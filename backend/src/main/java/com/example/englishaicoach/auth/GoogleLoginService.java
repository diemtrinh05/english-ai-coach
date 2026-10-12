package com.example.englishaicoach.auth;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.GoogleLoginRequest;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GoogleLoginService {
    private final GoogleIdentityVerifier verifier;
    private final GoogleAccountRepository accounts;
    private final AuthTokenIssuer tokens;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public GoogleLoginService(GoogleIdentityVerifier verifier, GoogleAccountRepository accounts,
            AuthTokenIssuer tokens, Clock clock, PlatformTransactionManager manager) {
        this.verifier = verifier;
        this.accounts = accounts;
        this.tokens = tokens;
        this.clock = clock;
        transaction = new TransactionTemplate(manager);
    }

    public AuthResponse login(GoogleLoginRequest request) {
        // HTTP/JWKS diễn ra trước transaction, không giữ row lock khi chờ provider.
        VerifiedGoogleIdentity identity = verifier.verify(request.idToken());
        return transaction.execute(status -> {
            var account = accounts.findLocked(identity.subject()).orElse(null);
            if (account == null) {
                if (identity.name() == null || identity.name().isBlank() || identity.name().length() > 100) {
                    throw GoogleAuthErrors.invalid();
                }
                accounts.insertIfFree(identity, clock.instant());
                // Sau ON CONFLICT, đọc lại snapshot để resolve winner cùng subject.
                account = accounts.findLocked(identity.subject()).orElseThrow(() ->
                        new ApiException(HttpStatus.CONFLICT, ApiErrorCodes.CONFLICT,
                                "Yêu cầu xung đột với trạng thái hiện tại."));
            }
            if (account.status() == UserStatus.LOCKED) {
                throw new ApiException(HttpStatus.LOCKED, ApiErrorCodes.AUTH_ACCOUNT_LOCKED,
                        "Tài khoản đang bị khóa. Vui lòng thử lại sau.");
            }
            accounts.recordLogin(account.id(), clock.instant());
            return tokens.issue(account.id(), account.email(), account.fullName(), account.role(),
                    account.status(), clock.instant());
        });
    }
}
