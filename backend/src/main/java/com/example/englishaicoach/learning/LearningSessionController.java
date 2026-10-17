package com.example.englishaicoach.learning;

import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.learning.dto.LearningSessionResponse;
import com.example.englishaicoach.learning.dto.StartLearningSessionRequest;
import com.example.englishaicoach.learning.dto.CompleteLearningSessionRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/v1/learning/sessions")
public class LearningSessionController {
    private static final Set<String> START_FIELDS = Set.of("sessionType", "eventId");

    private final LearningSessionService sessions;
    private final ObjectMapper mapper;
    private final Validator validator;

    public LearningSessionController(LearningSessionService sessions, ObjectMapper mapper, Validator validator) {
        this.sessions = sessions;
        this.mapper = mapper;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<LearningSessionResponse> start(
            @AuthenticationPrincipal AccessTokenIdentity identity,
            @RequestBody ObjectNode body,
            HttpServletRequest servletRequest) {
        if (body == null || body.properties().stream().anyMatch(field -> !START_FIELDS.contains(field.getKey()))
                || body.get("sessionType") == null || !body.get("sessionType").isTextual()
                || body.get("eventId") == null || !body.get("eventId").isTextual()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "Yêu cầu chứa trường không hợp lệ hoặc sai kiểu dữ liệu.");
        }
        StartLearningSessionRequest request;
        try {
            request = mapper.convertValue(body, StartLearningSessionRequest.class);
        } catch (JacksonException | IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "Yêu cầu không hợp lệ.");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        Map<String, List<String>> query = servletRequest.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Arrays.asList(entry.getValue())));
        var result = sessions.start(identity.userId(), request, query);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<LearningSessionResponse> complete(
            @AuthenticationPrincipal AccessTokenIdentity identity, @PathVariable UUID sessionId,
            @RequestBody ObjectNode body, HttpServletRequest servletRequest) {
        if (body == null || body.size() != 1 || body.get("eventId") == null
                || !body.get("eventId").isTextual()
                || !body.get("eventId").asText().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "Yêu cầu chỉ nhận eventId dạng UUID string.");
        }
        CompleteLearningSessionRequest request;
        try {
            request = mapper.convertValue(body, CompleteLearningSessionRequest.class);
        } catch (JacksonException | IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "eventId không hợp lệ.");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        Map<String, List<String>> query = servletRequest.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.asList(entry.getValue())));
        var result = sessions.complete(identity.userId(), sessionId, request, query);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @GetMapping("/{sessionId}")
    public LearningSessionResponse get(
            @AuthenticationPrincipal AccessTokenIdentity identity,
            @PathVariable UUID sessionId) {
        return sessions.get(identity.userId(), sessionId);
    }
}
