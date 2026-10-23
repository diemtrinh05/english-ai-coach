package com.example.englishaicoach.assessment;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Kết quả nội bộ để task persistence lưu trước khi công bố; không expose correctAnswer ra client. */
public record AssessmentQuestion(UUID vocabularyId, CefrDifficulty difficulty,
        String questionText, List<String> options, String correctAnswer) {
    public AssessmentQuestion {
        Objects.requireNonNull(vocabularyId, "vocabularyId");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(questionText, "questionText");
        Objects.requireNonNull(correctAnswer, "correctAnswer");
        options = List.copyOf(options);
        if (options.size() != 4 || options.stream().distinct().count() != 4
                || !options.contains(correctAnswer)) {
            throw new IllegalArgumentException("Câu hỏi phải có bốn lựa chọn khác nhau và một đáp án đúng");
        }
    }
}
