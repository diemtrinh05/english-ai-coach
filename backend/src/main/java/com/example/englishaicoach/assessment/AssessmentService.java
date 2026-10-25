package com.example.englishaicoach.assessment;

import com.example.englishaicoach.assessment.dto.AssessmentQuestionResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResultResponse;
import com.example.englishaicoach.assessment.dto.StartAssessmentRequest;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.idempotency.IdempotencyRequest;
import com.example.englishaicoach.common.idempotency.IdempotencyResult;
import com.example.englishaicoach.common.idempotency.IdempotencyService;
import com.example.englishaicoach.config.AssessmentProperties;
import java.time.Clock;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentService {
    public static final String ROUTE = "/api/v1/assessments";
    private final UserLevelAssessmentRepository assessments;
    private final AssessmentItemRepository items;
    private final AssessmentLookupRepository lookups;
    private final IdempotencyService idempotency;
    private final AssessmentQuestionSelector selector;
    private final Clock clock;

    public AssessmentService(UserLevelAssessmentRepository assessments, AssessmentItemRepository items,
            AssessmentLookupRepository lookups, IdempotencyService idempotency,
            AssessmentProperties properties, Clock clock) {
        this.assessments = assessments;
        this.items = items;
        this.lookups = lookups;
        this.idempotency = idempotency;
        this.selector = new AssessmentQuestionSelector(properties);
        this.clock = clock;
    }

    @Transactional
    public IdempotencyResult<AssessmentResponse> start(UUID userId, StartAssessmentRequest request) {
        if (!lookups.lockUser(userId)) throw unauthorized();
        // Replay đi trước mọi guard có thể thay đổi sau lần start đầu tiên.
        return idempotency.execute(userId, request.eventId(),
                new IdempotencyRequest("POST", ROUTE, ROUTE, Map.of(), request),
                AssessmentResponse.class, () -> {
                    if (assessments.findInProgressByUserId(userId).isPresent()) throw alreadyInProgress();
                    UUID id = UUID.randomUUID();
                    AssessmentQuestion question;
                    try {
                        question = selector.select(id, 1, CefrDifficulty.A1, lookups.catalogSnapshot(), Set.of());
                    } catch (AssessmentContentUnavailableException exception) {
                        throw contentUnavailable();
                    }
                    var assessment = assessments.insertInProgress(id, userId, request.type(),
                            UUID.randomUUID(), question, clock.instant()).orElseThrow(AssessmentService::alreadyInProgress);
                    return new IdempotencyResult<>(201, new AssessmentResponse(assessment.getId(),
                            assessment.getAssessmentType(), assessment.getStatus(), CefrDifficulty.A1));
                });
    }

    @Transactional(readOnly = true)
    public AssessmentResultResponse get(UUID userId, UUID id) {
        validateUser(userId);
        var assessment = assessments.findOwned(id, userId).orElseThrow(AssessmentService::notFound);
        var finalLevel = assessment.getStatus() == AssessmentStatus.COMPLETED
                && assessment.getFinalCefrLevelId() != null ? lookups.level(assessment.getFinalCefrLevelId()) : null;
        return new AssessmentResultResponse(id, assessment.getStatus(), assessment.getScore(),
                assessment.getQuestionsAnswered(), assessment.getCorrectAnswers(), finalLevel, assessment.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public AssessmentQuestionResponse next(UUID userId, UUID id) {
        validateUser(userId);
        var assessment = assessments.findOwned(id, userId).orElseThrow(AssessmentService::notFound);
        if (assessment.getStatus() != AssessmentStatus.IN_PROGRESS) throw notFound();
        var item = items.findFirstUnansweredOwned(id, userId).orElseThrow(AssessmentService::contentUnavailable);
        return new AssessmentQuestionResponse(item.getId(), item.getQuestionText(), item.getOptions(),
                lookups.level(item.getCefrLevelId()).code());
    }

    private void validateUser(UUID userId) {
        if (!lookups.userExists(userId)) throw unauthorized();
    }

    private static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.UNAUTHORIZED,
                "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, ApiErrorCodes.NOT_FOUND, "Không tìm thấy bài đánh giá hoặc câu hỏi.");
    }

    private static ApiException alreadyInProgress() {
        return new ApiException(HttpStatus.CONFLICT, ApiErrorCodes.ASSESSMENT_ALREADY_IN_PROGRESS,
                "Bạn đã có một bài đánh giá đang thực hiện.");
    }

    private static ApiException contentUnavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ApiErrorCodes.ASSESSMENT_CONTENT_UNAVAILABLE,
                "Nội dung đánh giá hiện chưa sẵn sàng. Vui lòng thử lại sau.");
    }
}
