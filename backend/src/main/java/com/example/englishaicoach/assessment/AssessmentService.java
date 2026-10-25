package com.example.englishaicoach.assessment;

import com.example.englishaicoach.assessment.dto.AssessmentQuestionResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResultResponse;
import com.example.englishaicoach.assessment.dto.StartAssessmentRequest;
import com.example.englishaicoach.assessment.dto.SubmitAssessmentAnswerRequest;
import com.example.englishaicoach.assessment.dto.AssessmentAnswerResponse;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.idempotency.IdempotencyRequest;
import com.example.englishaicoach.common.idempotency.IdempotencyResult;
import com.example.englishaicoach.common.idempotency.IdempotencyService;
import com.example.englishaicoach.config.AssessmentProperties;
import java.time.Clock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;
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
    private final AssessmentBlockCalculator blocks;
    private final Clock clock;

    public AssessmentService(UserLevelAssessmentRepository assessments, AssessmentItemRepository items,
            AssessmentLookupRepository lookups, IdempotencyService idempotency,
            AssessmentProperties properties, Clock clock) {
        this.assessments = assessments;
        this.items = items;
        this.lookups = lookups;
        this.idempotency = idempotency;
        this.selector = new AssessmentQuestionSelector(properties);
        this.blocks = new AssessmentBlockCalculator(properties);
        this.clock = clock;
    }

    @Transactional
    public IdempotencyResult<AssessmentAnswerResponse> answer(UUID userId, UUID id,
            SubmitAssessmentAnswerRequest request) {
        if (!lookups.lockUser(userId)) throw unauthorized();
        String route = ROUTE + "/{assessmentId}/answers";
        return idempotency.execute(userId, request.eventId(),
                new IdempotencyRequest("POST", route, ROUTE + "/" + id + "/answers", Map.of(), request),
                AssessmentAnswerResponse.class, () -> {
                    var assessment = assessments.lockOwned(id, userId).orElseThrow(AssessmentService::notFound);
                    var ordered = items.findOwnedInOrder(id, userId);
                    var item = ordered.stream().filter(candidate -> candidate.getId().equals(request.questionId()))
                            .findFirst().orElseThrow(AssessmentService::notFound);
                    // Event mới cho câu đã nhận luôn trả409, kể cả khi câu đó đã đóng phiên.
                    if (item.getAnsweredAt() != null) throw new ApiException(HttpStatus.CONFLICT,
                            ApiErrorCodes.ASSESSMENT_QUESTION_ALREADY_ANSWERED, "Câu hỏi này đã được trả lời.");
                    var first = ordered.stream().filter(candidate -> candidate.getAnsweredAt() == null).findFirst();
                    if (assessment.getStatus() != AssessmentStatus.IN_PROGRESS
                            || first.isEmpty() || !first.get().getId().equals(item.getId())
                            || !item.getOptions().contains(request.answer())) throw invalidAnswer();
                    boolean correct = item.getCorrectAnswer().equals(request.answer());
                    var previous = new AssessmentBlockState(CefrDifficulty.valueOf(lookups.level(assessment.getCurrentCefrLevelId()).code()),
                            assessment.getQuestionsAnswered(), assessment.getCorrectAnswers(),
                            assessment.getBlockQuestions(), assessment.getBlockCorrect(),
                            assessment.getStableBlockCount(), false);
                    var state = blocks.answer(previous, correct);
                    var now = clock.instant();
                    UUID difficultyId = lookups.levelId(state.difficulty());
                    BigDecimal score = BigDecimal.valueOf(state.correctAnswers()).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(state.questionsAnswered()), 2, RoundingMode.HALF_UP);
                    item.recordAnswer(request.answer(), correct, request.responseTimeMs(), now);
                    assessment.applyState(state, difficultyId, score, now);
                    if (state.completed()) {
                        lookups.updateExistingProfileLevel(userId, difficultyId, now);
                    } else {
                        AssessmentQuestion next;
                        try {
                            next = selector.select(id, state.questionsAnswered() + 1, state.difficulty(),
                                    lookups.catalogSnapshot(), ordered.stream().map(AssessmentItem::getVocabularyId)
                                            .collect(Collectors.toSet()));
                        } catch (AssessmentContentUnavailableException exception) {
                            throw contentUnavailable();
                        }
                        items.persist(AssessmentItem.unanswered(UUID.randomUUID(), id, state.questionsAnswered() + 1,
                                difficultyId, next, now));
                    }
                    return new IdempotencyResult<>(200, new AssessmentAnswerResponse(correct, state.difficulty(),
                            state.questionsAnswered(), state.completed()));
                });
    }

    private static ApiException invalidAnswer() {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                "Câu trả lời không thuộc lựa chọn hoặc bài đánh giá không còn nhận câu trả lời này.");
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
