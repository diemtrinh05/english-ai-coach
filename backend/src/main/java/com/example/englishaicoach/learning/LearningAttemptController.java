package com.example.englishaicoach.learning;

import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.learning.dto.LearningAttemptResponse;
import com.example.englishaicoach.learning.dto.SubmitLearningAttemptRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/v1/learning/attempts")
public class LearningAttemptController {
    private static final Set<String> TEXT = Set.of("sessionId", "vocabularyId", "attemptType", "eventId");
    private static final Set<String> INTEGER = Set.of("responseTimeMs", "answerQuality");
    private final LearningAttemptService service;
    private final ObjectMapper mapper;
    private final Validator validator;

    public LearningAttemptController(LearningAttemptService service, ObjectMapper mapper, Validator validator) {
        this.service = service;
        this.mapper = mapper;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<LearningAttemptResponse> submit(@AuthenticationPrincipal AccessTokenIdentity identity,
            @RequestBody ObjectNode body, HttpServletRequest servletRequest) {
        if (body == null || body.properties().stream().anyMatch(f -> !TEXT.contains(f.getKey()) && !INTEGER.contains(f.getKey()))
                || TEXT.stream().anyMatch(key -> body.get(key) == null || !body.get(key).isTextual())
                || INTEGER.stream().anyMatch(key -> body.get(key) == null || !body.get(key).isIntegralNumber()
                        || !body.get(key).canConvertToInt())) {
            throw invalid();
        }
        SubmitLearningAttemptRequest request;
        try {
            request = mapper.convertValue(body, SubmitLearningAttemptRequest.class);
        } catch (JacksonException | IllegalArgumentException exception) {
            throw invalid();
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        Map<String, List<String>> query = servletRequest.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.asList(entry.getValue())));
        var result = service.submit(identity.userId(), request, query);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                "Yêu cầu chứa trường không hợp lệ hoặc sai kiểu dữ liệu.");
    }
}
