package com.example.englishaicoach.vocabulary;

import java.util.UUID;

public record Vocabulary(UUID id, String word, String phoneticIpa, String meaningVi,
        String meaningEn, String partOfSpeech, String cefr, String audioUrl, String imageUrl) {
}
