package com.example.englishaicoach.vocabulary;

import com.example.englishaicoach.common.response.PaginatedResponse;
import com.example.englishaicoach.common.validation.PaginationRequest;
import com.example.englishaicoach.vocabulary.dto.VocabularyExampleResponse;
import com.example.englishaicoach.vocabulary.dto.VocabularyResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vocabulary")
public class VocabularyController {

    private final VocabularyService service;

    public VocabularyController(VocabularyService service) {
        this.service = service;
    }

    @GetMapping
    public PaginatedResponse<VocabularyResponse> listVocabulary(
            @Valid @ModelAttribute PaginationRequest pagination,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cefr,
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) String partOfSpeech,
            @RequestParam(required = false) String sort) {
        return service.listVocabulary(pagination.page(), pagination.size(), search,
                cefr, topicId, partOfSpeech, sort);
    }

    @GetMapping("/{vocabularyId}")
    public VocabularyResponse getVocabulary(@PathVariable UUID vocabularyId) {
        return service.getVocabulary(vocabularyId);
    }

    @GetMapping("/{vocabularyId}/examples")
    public List<VocabularyExampleResponse> getVocabularyExamples(@PathVariable UUID vocabularyId) {
        return service.getVocabularyExamples(vocabularyId);
    }
}
