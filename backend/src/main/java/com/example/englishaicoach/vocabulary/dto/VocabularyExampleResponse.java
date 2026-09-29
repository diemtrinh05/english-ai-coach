package com.example.englishaicoach.vocabulary.dto;

import java.util.UUID;

public record VocabularyExampleResponse(UUID id, String exampleText,
        String translationText, String source) {
}
