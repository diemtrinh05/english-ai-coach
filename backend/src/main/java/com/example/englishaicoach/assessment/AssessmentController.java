package com.example.englishaicoach.assessment;

import com.example.englishaicoach.assessment.dto.AssessmentQuestionResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResultResponse;
import com.example.englishaicoach.assessment.dto.StartAssessmentRequest;
import com.example.englishaicoach.assessment.dto.SubmitAssessmentAnswerRequest;
import com.example.englishaicoach.assessment.dto.AssessmentAnswerResponse;
import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping(AssessmentService.ROUTE)
public class AssessmentController {
    private static final String UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private final AssessmentService assessments;
    private final ObjectMapper mapper;

    public AssessmentController(AssessmentService assessments, ObjectMapper mapper) {
        this.assessments = assessments;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<AssessmentResponse> start(@AuthenticationPrincipal AccessTokenIdentity identity,
            @RequestBody String rawBody, HttpServletRequest request) {
        rejectQuery(request);
        StartAssessmentRequest body;
        try {
            ObjectNode node = mapper.readerFor(ObjectNode.class)
                    .with(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY,
                            DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readValue(rawBody);
            if (node == null || node.size() != 2 || node.get("type") == null || !node.get("type").isTextual()
                    || node.get("eventId") == null || !node.get("eventId").isTextual()
                    || !node.get("eventId").asText().matches(UUID_PATTERN)) throw invalid();
            body = new StartAssessmentRequest(AssessmentType.valueOf(node.get("type").asText()),
                    UUID.fromString(node.get("eventId").asText()));
        } catch (JacksonException | IllegalArgumentException exception) {
            throw invalid();
        }
        var result = assessments.start(identity.userId(), body);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @GetMapping("/{assessmentId}")
    public AssessmentResultResponse get(@AuthenticationPrincipal AccessTokenIdentity identity,
            @PathVariable String assessmentId, HttpServletRequest request) {
        rejectQuery(request);
        return assessments.get(identity.userId(), uuid(assessmentId));
    }

    @PostMapping("/{assessmentId}/answers")
    public ResponseEntity<AssessmentAnswerResponse> answer(@AuthenticationPrincipal AccessTokenIdentity identity,
            @PathVariable String assessmentId, @RequestBody String rawBody, HttpServletRequest request) {
        rejectQuery(request);
        UUID id = uuid(assessmentId);
        SubmitAssessmentAnswerRequest body;
        try {
            ObjectNode node = mapper.readerFor(ObjectNode.class)
                    .with(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY,
                            DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readValue(rawBody);
            if (node == null || node.size() != 4
                    || node.get("questionId") == null || !node.get("questionId").isTextual()
                    || node.get("eventId") == null || !node.get("eventId").isTextual()
                    || node.get("answer") == null || !node.get("answer").isTextual()
                    || node.get("responseTimeMs") == null || !node.get("responseTimeMs").isIntegralNumber()
                    || !node.get("responseTimeMs").canConvertToInt()
                    || node.get("responseTimeMs").intValue() < 0) throw invalid();
            body = new SubmitAssessmentAnswerRequest(uuid(node.get("questionId").asText()),
                    node.get("answer").asText(), node.get("responseTimeMs").intValue(),
                    uuid(node.get("eventId").asText()));
        } catch (JacksonException | IllegalArgumentException exception) {
            throw invalid();
        }
        var result = assessments.answer(identity.userId(), id, body);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @GetMapping("/{assessmentId}/next-question")
    public AssessmentQuestionResponse next(@AuthenticationPrincipal AccessTokenIdentity identity,
            @PathVariable String assessmentId, HttpServletRequest request) {
        rejectQuery(request);
        return assessments.next(identity.userId(), uuid(assessmentId));
    }

    private static UUID uuid(String value) {
        if (!value.matches(UUID_PATTERN)) throw invalid();
        return UUID.fromString(value);
    }

    private static void rejectQuery(HttpServletRequest request) {
        if (!request.getParameterMap().isEmpty()) throw invalid();
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                "Yêu cầu đánh giá chứa trường hoặc kiểu dữ liệu không hợp lệ.");
    }
}
