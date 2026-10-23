package com.example.englishaicoach.assessment;

import java.util.Objects;
import java.util.UUID;

/** Dữ liệu catalog đầu vào; caller chịu trách nhiệm lấy snapshot nhất quán. */
public record AssessmentVocabulary(UUID id, CefrDifficulty difficulty, String word,
        String partOfSpeech, String meaningVi, boolean active) {
    public AssessmentVocabulary {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(difficulty, "difficulty");
    }

    public boolean usable() {
        return active && nonblank(word) && nonblank(partOfSpeech) && nonblank(meaningVi);
    }

    private static boolean nonblank(String value) {
        return value != null && !value.isBlank();
    }
}
