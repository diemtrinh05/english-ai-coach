package com.example.englishaicoach.assessment;

import com.example.englishaicoach.config.AssessmentProperties;
import java.util.Objects;

/** Chuyển trạng thái block thuần, không I/O, clock, lưu câu trả lời hoặc phát thưởng. */
public final class AssessmentBlockCalculator {
    private final AssessmentProperties properties;

    public AssessmentBlockCalculator(AssessmentProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public AssessmentBlockState initialState() {
        return new AssessmentBlockState(CefrDifficulty.valueOf(properties.startingLevel()),
                0, 0, 0, 0, 0, false);
    }

    public AssessmentBlockState answer(AssessmentBlockState state, boolean correct) {
        Objects.requireNonNull(state, "state");
        if (state.completed()) {
            throw new IllegalArgumentException("Assessment đã hoàn tất");
        }
        int total = state.questionsAnswered() + 1;
        int correctTotal = state.correctAnswers() + (correct ? 1 : 0);
        int blockQuestions = state.blockQuestions() + 1;
        int blockCorrect = state.blockCorrect() + (correct ? 1 : 0);
        int stable = state.stableBlockCount();
        CefrDifficulty difficulty = state.difficulty();
        if (blockQuestions == properties.blockSize()) {
            int step = blockCorrect >= properties.promoteCorrectThreshold() ? 1
                    : blockCorrect <= properties.demoteCorrectThreshold() ? -1 : 0;
            CefrDifficulty next = difficulty.move(step);
            // PROMOTE/DEMOTE bị clamp tại biên là HOLD, như owner đã phê duyệt.
            stable = next == difficulty ? stable + 1 : 0;
            difficulty = next;
            blockQuestions = 0;
            blockCorrect = 0;
        }
        boolean completed = total >= properties.maximumQuestions()
                || total >= properties.minimumQuestions() && stable >= properties.stableHoldBlocksRequired();
        return new AssessmentBlockState(difficulty, total, correctTotal, blockQuestions,
                blockCorrect, stable, completed);
    }
}
