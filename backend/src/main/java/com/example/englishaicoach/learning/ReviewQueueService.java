package com.example.englishaicoach.learning;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.learning.dto.ReviewQueueResponse;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewQueueService {
    private final ReviewQueueRepository reviews;
    private final Clock clock;

    public ReviewQueueService(ReviewQueueRepository reviews, Clock clock) {
        this.reviews = reviews;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReviewQueueResponse get(UUID userId, int limit, UUID topicId) {
        if (!reviews.userExists(userId)) throw new ApiException(HttpStatus.UNAUTHORIZED,
                ApiErrorCodes.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
        var items = reviews.findDue(userId, clock.instant(), topicId, limit);
        return new ReviewQueueResponse(items, items.size());
    }
}
