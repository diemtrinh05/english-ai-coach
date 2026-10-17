package com.example.englishaicoach.progress;

import com.example.englishaicoach.auth.AccessTokenIdentity;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.learning.dto.VocabularyProgressResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VocabularyProgressController {
    private final VocabularyProgressService progress;

    public VocabularyProgressController(VocabularyProgressService progress) { this.progress = progress; }

    @GetMapping("/api/v1/vocabulary/{vocabularyId}/progress")
    public VocabularyProgressResponse get(@AuthenticationPrincipal AccessTokenIdentity identity,
            @PathVariable String vocabularyId, HttpServletRequest request) {
        if (!request.getParameterMap().isEmpty()
                || !vocabularyId.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "Tham số yêu cầu không hợp lệ.");
        }
        return progress.get(identity.userId(), UUID.fromString(vocabularyId));
    }
}
