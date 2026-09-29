package com.example.englishaicoach.vocabulary;

import com.example.englishaicoach.common.response.PaginatedResponse;
import com.example.englishaicoach.common.validation.PaginationRequest;
import com.example.englishaicoach.vocabulary.dto.TopicResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {

    private final TopicService service;

    public TopicController(TopicService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<TopicResponse> listTopics(@Valid @ModelAttribute PaginationRequest pagination,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID parentTopicId) {
        return service.listTopics(pagination.page(), pagination.size(), search, parentTopicId);
    }

    @GetMapping("/{topicId}")
    public TopicResponse getTopic(@PathVariable UUID topicId) {
        return service.getTopic(topicId);
    }
}
