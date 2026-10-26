package com.example.englishaicoach.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.englishaicoach.config.AssessmentProperties;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Oracle dạng bảng literal từ assessment-block-v1, độc lập với calculator production. */
class AssessmentQaBlockMatrixTests {
    private static final String[][] EXPECTED_LEVELS = {
        {"A1", "A1", "A1", "A2", "A2"},
        {"A1", "A1", "A2", "B1", "B1"},
        {"A2", "A2", "B1", "B2", "B2"},
        {"B1", "B1", "B2", "C1", "C1"},
        {"B2", "B2", "C1", "C2", "C2"},
        {"C1", "C1", "C2", "C2", "C2"}
    };
    private static final int[][] EXPECTED_STABILITY = {
        {1, 1, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0},
        {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 1, 1}
    };

    @ParameterizedTest(name = "{0}, pattern={1}")
    @MethodSource("everyFourAnswerPermutationAtEveryLevel")
    void blockOutcomeIsIndependentOfCorrectAnswerPositions(CefrDifficulty initial, int pattern,
            String expectedLevel, int expectedStable) {
        var calculator = new AssessmentBlockCalculator(new AssessmentProperties(
                "assessment-block-v1", 20, 50, 4, 3, 1, 2, "A1", 4, 30));
        var state = new AssessmentBlockState(initial, 0, 0, 0, 0, 0, false);
        int observedCorrect = 0;
        for (int position = 0; position < 4; position++) {
            boolean answer = (pattern & (1 << position)) != 0;
            if (answer) observedCorrect++;
            state = calculator.answer(state, answer);
            assertThat(state.questionsAnswered()).isEqualTo(position + 1);
            assertThat(state.correctAnswers()).isEqualTo(observedCorrect);
            assertThat(state.completed()).isFalse();
            if (position < 3) {
                assertThat(state.difficulty()).isEqualTo(initial);
                assertThat(state.blockQuestions()).isEqualTo(position + 1);
                assertThat(state.blockCorrect()).isEqualTo(observedCorrect);
                assertThat(state.stableBlockCount()).isZero();
            }
        }
        assertThat(state.difficulty().name()).isEqualTo(expectedLevel);
        assertThat(state.stableBlockCount()).isEqualTo(expectedStable);
        assertThat(state.blockQuestions()).isZero();
        assertThat(state.blockCorrect()).isZero();
    }

    static Stream<Arguments> everyFourAnswerPermutationAtEveryLevel() {
        return Stream.of(CefrDifficulty.values()).flatMap(level -> IntStream.range(0, 16)
                .mapToObj(pattern -> Arguments.of(level, pattern,
                        EXPECTED_LEVELS[level.ordinal()][Integer.bitCount(pattern)],
                        EXPECTED_STABILITY[level.ordinal()][Integer.bitCount(pattern)])));
    }
}
