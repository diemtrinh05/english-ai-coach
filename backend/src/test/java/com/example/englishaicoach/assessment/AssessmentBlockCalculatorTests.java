package com.example.englishaicoach.assessment;

import static org.junit.jupiter.api.Assertions.*;

import com.example.englishaicoach.config.AssessmentProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AssessmentBlockCalculatorTests {
    static AssessmentProperties properties() {
        return new AssessmentProperties("assessment-block-v1", 20, 50, 4, 3, 1, 2, "A1", 4, 30);
    }

    private final AssessmentBlockCalculator calculator = new AssessmentBlockCalculator(properties());

    @Test
    void startsAtA1WithEmptyCounters() {
        assertEquals(new AssessmentBlockState(CefrDifficulty.A1, 0, 0, 0, 0, 0, false), calculator.initialState());
    }

    @ParameterizedTest
    @CsvSource({"0,A1,0", "1,A1,0", "2,A2,1", "3,B1,0", "4,B1,0"})
    void evaluatesEveryBlockScoreAndResetsBlockCounters(int correct, CefrDifficulty next, int stable) {
        AssessmentBlockState state = block(new AssessmentBlockState(CefrDifficulty.A2, 0, 0, 0, 0, 0, false), correct);
        assertEquals(next, state.difficulty());
        assertEquals(stable, state.stableBlockCount());
        assertEquals(correct, state.correctAnswers());
        assertEquals(4, state.questionsAnswered());
        assertEquals(0, state.blockQuestions());
        assertEquals(0, state.blockCorrect());
    }

    @Test
    void floorAndCeilingClampCountAsStableHolds() {
        AssessmentBlockState floor = block(calculator.initialState(), 0);
        AssessmentBlockState ceiling = block(new AssessmentBlockState(CefrDifficulty.C2, 0, 0, 0, 0, 0, false), 4);
        assertEquals(CefrDifficulty.A1, floor.difficulty());
        assertEquals(CefrDifficulty.C2, ceiling.difficulty());
        assertEquals(1, floor.stableBlockCount());
        assertEquals(1, ceiling.stableBlockCount());
    }

    @Test
    void doesNotMoveDifficultyUntilFourthAnswer() {
        AssessmentBlockState state = calculator.initialState();
        for (int index = 1; index <= 3; index++) {
            state = calculator.answer(state, true);
            assertEquals(CefrDifficulty.A1, state.difficulty());
            assertEquals(index, state.blockQuestions());
            assertEquals(index, state.blockCorrect());
            assertEquals(0, state.stableBlockCount());
        }
        assertEquals(CefrDifficulty.A2, calculator.answer(state, false).difficulty());
    }

    @Test
    void completesAtTwentyButNeverBeforeMinimumDespiteStableBlocks() {
        AssessmentBlockState state = calculator.initialState();
        for (int index = 1; index <= 20; index++) {
            state = calculator.answer(state, false);
            assertEquals(index == 20, state.completed());
        }
        assertEquals(5, state.stableBlockCount());
        assertEquals(CefrDifficulty.A1, state.difficulty());
        AssessmentBlockState completed = state;
        assertThrows(IllegalArgumentException.class, () -> calculator.answer(completed, true));
    }

    @Test
    void levelChangesResetStableAndTwoNewHoldsAreRequiredAfterward() {
        AssessmentBlockState state = calculator.initialState();
        for (int correct : new int[] {2, 2, 3, 2, 2}) {
            state = block(state, correct);
        }
        assertTrue(state.completed());
        assertEquals(20, state.questionsAnswered());
        // Một chuỗi khác kết thúc block20 bằng đổi level nên phải chờ thêm hai HOLD.
        state = calculator.initialState();
        for (int correct : new int[] {3, 3, 3, 2, 3}) {
            state = block(state, correct);
        }
        assertEquals(0, state.stableBlockCount());
        assertFalse(state.completed());
        state = block(state, 2);
        assertFalse(state.completed());
        state = block(state, 2);
        assertEquals(28, state.questionsAnswered());
        assertTrue(state.completed());
    }

    @Test
    void fiftyStopsPartialBlockWithoutMovingDifficultyOrInventingHold() {
        AssessmentBlockState state = calculator.initialState();
        for (int index = 0; index < 12; index++) {
            state = block(state, index % 2 == 0 ? 3 : 1);
        }
        assertEquals(48, state.questionsAnswered());
        assertEquals(CefrDifficulty.A1, state.difficulty());
        assertFalse(state.completed());
        state = calculator.answer(state, true);
        assertFalse(state.completed());
        state = calculator.answer(state, true);
        assertTrue(state.completed());
        assertEquals(50, state.questionsAnswered());
        assertEquals(CefrDifficulty.A1, state.difficulty());
        assertEquals(0, state.stableBlockCount());
        assertEquals(2, state.blockQuestions());
        assertEquals(2, state.blockCorrect());
    }

    @Test
    void rejectsInconsistentSnapshots() {
        assertThrows(IllegalArgumentException.class,
                () -> new AssessmentBlockState(CefrDifficulty.A1, 3, 4, 3, 3, 0, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AssessmentBlockState(CefrDifficulty.A1, 4, 0, 1, 0, 0, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AssessmentBlockState(CefrDifficulty.A1, 20, 10, 0, 0, 2, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AssessmentBlockState(CefrDifficulty.A1, 50, 10, 2, 0, 0, false));
    }

    private AssessmentBlockState block(AssessmentBlockState state, int correct) {
        for (int index = 0; index < 4; index++) {
            state = calculator.answer(state, index < correct);
        }
        return state;
    }
}
