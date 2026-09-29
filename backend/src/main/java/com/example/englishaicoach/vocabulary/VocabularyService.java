package com.example.englishaicoach.vocabulary;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.response.PaginatedResponse;
import com.example.englishaicoach.vocabulary.dto.TopicResponse;
import com.example.englishaicoach.vocabulary.dto.VocabularyExampleResponse;
import com.example.englishaicoach.vocabulary.dto.VocabularyResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VocabularyService {

    private static final Set<String> CEFR_CODES = Set.of("A1", "A2", "B1", "B2", "C1", "C2");
    private final VocabularyRepository repository;

    public VocabularyService(VocabularyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<VocabularyResponse> listVocabulary(int page, int size,
            String search, String cefr, UUID topicId, String partOfSpeech, String sort) {
        String normalizedCefr = normalize(cefr);
        if (normalizedCefr != null && !CEFR_CODES.contains(normalizedCefr)) {
            throw invalid("cefr không hợp lệ.");
        }
        String orderBy = orderBy(sort);
        String normalizedSearch = normalize(search);
        String normalizedPartOfSpeech = normalize(partOfSpeech);
        long total = repository.countActive(normalizedSearch, normalizedCefr, topicId,
                normalizedPartOfSpeech);
        long offset = (long) page * size;
        List<Vocabulary> words = repository.findActive(normalizedSearch, normalizedCefr,
                topicId, normalizedPartOfSpeech, orderBy, size, offset);
        List<VocabularyResponse> content = responses(words);
        int totalPages = (int) ((total + size - 1) / size);
        return new PaginatedResponse<>(content, page, size, total, totalPages,
                (long) page + 1 < totalPages);
    }

    @Transactional(readOnly = true)
    public VocabularyResponse getVocabulary(UUID id) {
        Vocabulary word = repository.findActiveById(id).orElseThrow(this::notFound);
        return responses(List.of(word)).getFirst();
    }

    @Transactional(readOnly = true)
    public List<VocabularyExampleResponse> getVocabularyExamples(UUID id) {
        repository.findActiveById(id).orElseThrow(this::notFound);
        return List.copyOf(repository.findExamples(List.of(id)).getOrDefault(id, List.of()));
    }

    private List<VocabularyResponse> responses(List<Vocabulary> words) {
        List<UUID> ids = words.stream().map(Vocabulary::id).toList();
        Map<UUID, List<TopicResponse>> topics = repository.findTopics(ids);
        Map<UUID, List<VocabularyExampleResponse>> examples = repository.findExamples(ids);
        return words.stream().map(word -> new VocabularyResponse(word.id(), word.word(),
                word.phoneticIpa(), word.meaningVi(), word.meaningEn(), word.partOfSpeech(),
                word.cefr(), topics.getOrDefault(word.id(), List.of()), word.audioUrl(),
                word.imageUrl(), examples.getOrDefault(word.id(), List.of()))).toList();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String orderBy(String sort) {
        if (sort == null || sort.isBlank() || sort.equals("word,asc")) {
            return "LOWER(v.word) ASC";
        }
        return switch (sort) {
            case "word,desc" -> "LOWER(v.word) DESC";
            case "createdAt,asc" -> "v.created_at ASC";
            case "createdAt,desc" -> "v.created_at DESC";
            default -> throw invalid("sort không hợp lệ.");
        };
    }

    private ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR, message);
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, ApiErrorCodes.NOT_FOUND,
                "Không tìm thấy từ vựng.");
    }
}
