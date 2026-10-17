package com.example.englishaicoach.progress;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.config.SrsProperties;
import com.example.englishaicoach.learning.LearningStatus;
import com.example.englishaicoach.learning.dto.VocabularyProgressResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VocabularyProgressService {
    private final VocabularyProgressRepository progress;
    private final SrsProperties srs;

    public VocabularyProgressService(VocabularyProgressRepository progress, SrsProperties srs) {
        this.progress = progress;
        this.srs = srs;
    }

    @Transactional(readOnly = true)
    public VocabularyProgressResponse get(UUID userId, UUID vocabularyId) {
        if (!progress.userExists(userId)) throw new ApiException(HttpStatus.UNAUTHORIZED,
                ApiErrorCodes.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
        if (!progress.activeVocabularyExists(vocabularyId)) throw new ApiException(HttpStatus.NOT_FOUND,
                ApiErrorCodes.NOT_FOUND, "Không tìm thấy từ vựng.");
        // Snapshot NEW chỉ để đọc; mutation learning mới được tạo current state trong DB.
        return progress.find(userId, vocabularyId).orElseGet(() -> new VocabularyProgressResponse(
                vocabularyId, LearningStatus.NEW, srs.initialEaseFactor(), 0, 0, null, 0, 0));
    }
}
