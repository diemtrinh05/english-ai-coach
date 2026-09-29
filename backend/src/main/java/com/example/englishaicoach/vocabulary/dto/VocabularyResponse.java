package com.example.englishaicoach.vocabulary.dto;

import java.util.List;
import java.util.UUID;

public record VocabularyResponse(UUID id, String word, String phoneticIpa, String meaningVi,
        String meaningEn, String partOfSpeech, String cefr, List<TopicResponse> topics,
        String audioUrl, String imageUrl, List<VocabularyExampleResponse> examples) {
    public VocabularyResponse {
        topics = List.copyOf(topics);
        examples = List.copyOf(examples);
    }
}
