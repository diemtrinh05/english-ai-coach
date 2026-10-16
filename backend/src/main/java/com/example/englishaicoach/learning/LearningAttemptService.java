package com.example.englishaicoach.learning;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.idempotency.IdempotencyRequest;
import com.example.englishaicoach.common.idempotency.IdempotencyResult;
import com.example.englishaicoach.common.idempotency.IdempotencyService;
import com.example.englishaicoach.gamification.GamificationService;
import com.example.englishaicoach.gamification.StreakActivity;
import com.example.englishaicoach.gamification.StreakService;
import com.example.englishaicoach.gamification.XpReason;
import com.example.englishaicoach.gamification.XpSource;
import com.example.englishaicoach.learning.dto.LearningAttemptResponse;
import com.example.englishaicoach.learning.dto.SubmitLearningAttemptRequest;
import com.example.englishaicoach.learning.dto.VocabularyProgressResponse;
import com.example.englishaicoach.learning.srs.SrsCalculationInput;
import com.example.englishaicoach.learning.srs.SrsService;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningAttemptService {
    public static final String ROUTE = "/api/v1/learning/attempts";
    private final LearningSessionRepository sessions;
    private final LearningAttemptRepository attempts;
    private final IdempotencyService idempotency;
    private final SrsService srs;
    private final GamificationService gamification;
    private final StreakService streak;
    private final Clock clock;

    public LearningAttemptService(LearningSessionRepository sessions, LearningAttemptRepository attempts,
            IdempotencyService idempotency, SrsService srs, GamificationService gamification,
            StreakService streak, Clock clock) {
        this.sessions = sessions;
        this.attempts = attempts;
        this.idempotency = idempotency;
        this.srs = srs;
        this.gamification = gamification;
        this.streak = streak;
        this.clock = clock;
    }

    @Transactional
    public IdempotencyResult<LearningAttemptResponse> submit(UUID userId, SubmitLearningAttemptRequest request,
            Map<String, List<String>> query) {
        if (!sessions.lockUser(userId)) throw new ApiException(HttpStatus.UNAUTHORIZED,
                ApiErrorCodes.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ.");
        return idempotency.execute(userId, request.eventId(),
                new IdempotencyRequest("POST", ROUTE, ROUTE, query, request), LearningAttemptResponse.class,
                () -> accept(userId, request));
    }

    private IdempotencyResult<LearningAttemptResponse> accept(UUID userId, SubmitLearningAttemptRequest request) {
        var ended = attempts.lockOwnedSession(request.sessionId(), userId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, ApiErrorCodes.NOT_FOUND, "Không tìm thấy phiên học."));
        if (ended.closed()) throw new ApiException(HttpStatus.BAD_REQUEST,
                ApiErrorCodes.VALIDATION_ERROR, "Phiên học đã kết thúc.");
        if (!attempts.lockActiveVocabulary(request.vocabularyId())) throw new ApiException(HttpStatus.NOT_FOUND,
                ApiErrorCodes.NOT_FOUND, "Không tìm thấy từ vựng đang hoạt động.");
        var now = clock.instant();
        var progress = attempts.loadOrInitialize(userId, request.vocabularyId(), now);
        var oldEase = progress.getEaseFactor();
        int oldInterval = progress.getIntervalDays();
        int oldRepetitions = progress.getRepetitions();
        var transition = srs.calculate(progress.getStatus(), new SrsCalculationInput(oldEase, oldInterval,
                oldRepetitions, request.answerQuality(), request.responseTimeMs(), now));
        var result = transition.calculation();
        progress.apply(transition, request.answerQuality(), now);
        UUID attemptId = UUID.randomUUID();
        attempts.record(attemptId, request.sessionId(), request.vocabularyId(), request.attemptType(),
                result.isCorrect(), request.responseTimeMs(), request.answerQuality(), now);
        progress.updateAverageResponseTime(attempts.averageResponseTime(userId, request.vocabularyId()));
        // Flush @Version trước XP/streak; conflict rollback cả attempt vừa ghi.
        attempts.flush();
        attempts.updateSessionAggregates(request.sessionId());
        if (result.isCorrect()) gamification.award(XpSource.entity(userId, XpReason.LEARNING_CORRECT, attemptId));
        streak.recordAcceptedActivity(userId, StreakActivity.SESSION_ATTEMPT);
        var response = new LearningAttemptResponse(attemptId, result.isCorrect(), request.answerQuality(),
                new LearningAttemptResponse.SrsResult(oldEase, result.newEaseFactor(), oldInterval,
                        result.newIntervalDays(), oldRepetitions, result.newRepetitions(), result.nextReviewAt(),
                        result.algorithmVersion()),
                new VocabularyProgressResponse(request.vocabularyId(), progress.getStatus(), progress.getEaseFactor(),
                        progress.getIntervalDays(), progress.getRepetitions(), progress.getNextReviewAt(),
                        progress.getCorrectCount(), progress.getIncorrectCount()));
        return new IdempotencyResult<>(200, response);
    }
}
