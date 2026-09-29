package com.example.englishaicoach.vocabulary;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.response.PaginatedResponse;
import com.example.englishaicoach.vocabulary.dto.TopicResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicService {

    private final TopicRepository repository;

    public TopicService(TopicRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<TopicResponse> listTopics(int page, int size,
            String search, UUID parentTopicId) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        long total = repository.countActive(normalizedSearch, parentTopicId);
        long offset = (long) page * size;
        List<TopicResponse> content = repository.findActive(normalizedSearch, parentTopicId, size, offset)
                .stream().map(this::toResponse).toList();
        int totalPages = (int) ((total + size - 1) / size);
        return new PaginatedResponse<>(content, page, size, total, totalPages,
                (long) page + 1 < totalPages);
    }

    @Transactional(readOnly = true)
    public TopicResponse getTopic(UUID topicId) {
        return repository.findActiveById(topicId)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        ApiErrorCodes.NOT_FOUND, "Không tìm thấy topic."));
    }

    private TopicResponse toResponse(Topic topic) {
        return new TopicResponse(topic.id(), topic.name(), topic.description(),
                topic.iconUrl(), topic.parentTopicId(), topic.isActive());
    }
}
