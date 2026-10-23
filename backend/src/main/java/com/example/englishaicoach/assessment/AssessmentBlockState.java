package com.example.englishaicoach.assessment;

import java.util.Objects;

/** Snapshot thuần; không phải aggregate/JPA hoặc DTO trả cho client. */
public record AssessmentBlockState(CefrDifficulty difficulty, int questionsAnswered,
        int correctAnswers, int blockQuestions, int blockCorrect, int stableBlockCount,
        boolean completed) {

    public AssessmentBlockState {
        Objects.requireNonNull(difficulty, "difficulty");
        if (questionsAnswered < 0 || questionsAnswered > 50
                || correctAnswers < 0 || correctAnswers > questionsAnswered
                || blockQuestions != questionsAnswered % 4
                || blockCorrect < 0 || blockCorrect > blockQuestions
                || blockCorrect > correctAnswers
                || correctAnswers - blockCorrect > questionsAnswered - blockQuestions
                || stableBlockCount < 0 || stableBlockCount > questionsAnswered / 4
                || completed != (questionsAnswered == 50
                        || questionsAnswered >= 20 && stableBlockCount >= 2)) {
            throw new IllegalArgumentException("Snapshot block assessment không hợp lệ");
        }
    }
}
