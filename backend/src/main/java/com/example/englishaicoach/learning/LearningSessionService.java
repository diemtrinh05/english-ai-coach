package com.example.englishaicoach.learning;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.idempotency.IdempotencyRequest;
import com.example.englishaicoach.common.idempotency.IdempotencyResult;
import com.example.englishaicoach.common.idempotency.IdempotencyService;
import com.example.englishaicoach.learning.dto.LearningSessionResponse;
import com.example.englishaicoach.learning.dto.StartLearningSessionRequest;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningSessionService {
    public static final String ROUTE = "/api/v1/learning/sessions";

    private final LearningSessionRepository sessions;
    private final IdempotencyService idempotency;
    private final Clock clock;

    public LearningSessionService(LearningSessionRepository sessions, IdempotencyService idempotency, Clock clock) {
        this.sessions = sessions;
        this.idempotency = idempotency;
        this.clock = clock;
    }

    @Transactional
    public IdempotencyResult<LearningSessionResponse> start(
            UUID userId, StartLearningSessionRequest request, Map<String, List<String>> query) {
        if (!sessions.lockUser(userId)) {
            throw unauthorized();
        }
        return idempotency.execute(userId, request.eventId(),
                new IdempotencyRequest("POST", ROUTE, ROUTE, query, request),
                LearningSessionResponse.class,
                () -> {
                    UUID sessionId = UUID.randomUUID();
                    var response = new LearningSessionResponse(
                            sessionId, request.sessionType(), clock.instant(), null, 0, null);
                    sessions.insert(sessionId, userId, request.sessionType(), response.startedAt());
                    return new IdempotencyResult<>(201, response);
                });
    }

    @Transactional(readOnly = true)
    public LearningSessionResponse get(UUID userId, UUID sessionId) {
        if (!sessions.userExists(userId)) {
            throw unauthorized();
        }
        return sessions.findOwned(sessionId, userId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, ApiErrorCodes.NOT_FOUND, "Không tìm thấy phiên học."));
    }

    private static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.UNAUTHORIZED,
                "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
    }
}
