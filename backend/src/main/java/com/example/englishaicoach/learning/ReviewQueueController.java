package com.example.englishaicoach.learning;

import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.learning.dto.ReviewQueueResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewQueueController {
    private final ReviewQueueService reviews;

    public ReviewQueueController(ReviewQueueService reviews) { this.reviews = reviews; }

    @GetMapping("/api/v1/learning/reviews")
    public ReviewQueueResponse get(@AuthenticationPrincipal AccessTokenIdentity identity,
            HttpServletRequest request) {
        var query = request.getParameterMap();
        if (query.entrySet().stream().anyMatch(entry -> !Set.of("limit", "topicId").contains(entry.getKey())
                || entry.getValue().length != 1)) throw invalid();
        int limit = 50;
        UUID topicId = null;
        try {
            if (query.containsKey("limit")) {
                String value = query.get("limit")[0];
                if (!value.matches("[0-9]+")) throw invalid();
                limit = Integer.parseInt(value);
                if (limit < 1 || limit > 100) throw invalid();
            }
            if (query.containsKey("topicId")) {
                String value = query.get("topicId")[0];
                if (!value.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) throw invalid();
                topicId = UUID.fromString(value);
            }
        } catch (IllegalArgumentException exception) {
            throw invalid();
        }
        return reviews.get(identity.userId(), limit, topicId);
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                "Tham số truy vấn không hợp lệ.");
    }
}
