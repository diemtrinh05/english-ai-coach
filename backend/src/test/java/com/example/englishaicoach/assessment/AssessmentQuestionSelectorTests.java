package com.example.englishaicoach.assessment;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AssessmentQuestionSelectorTests {
    private static final UUID ASSESSMENT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final AssessmentQuestionSelector selector = new AssessmentQuestionSelector(AssessmentBlockCalculatorTests.properties());

    @ParameterizedTest
    @EnumSource(CefrDifficulty.class)
    void selectsOnlyRequestedLevelWithFourDistinctSameLevelOptions(CefrDifficulty level) {
        List<AssessmentVocabulary> catalog = catalog();
        AssessmentQuestion question = selector.select(ASSESSMENT, 1, level, catalog, Set.of());
        AssessmentVocabulary target = catalog.stream().filter(word -> word.id().equals(question.vocabularyId())).findFirst().orElseThrow();
        assertEquals(level, question.difficulty());
        assertEquals(target.meaningVi(), question.correctAnswer());
        assertEquals("Từ '" + target.word() + "' có nghĩa là gì?", question.questionText());
        assertEquals(4, new HashSet<>(question.options()).size());
        assertTrue(question.options().stream().allMatch(option -> option.startsWith(level + " nghĩa ")));
        assertThrows(UnsupportedOperationException.class, () -> question.options().add("khác"));
    }

    @Test
    void matchesFrozenSha256QuestionAndOptionOrder() {
        AssessmentQuestion question = selector.select(ASSESSMENT, 1, CefrDifficulty.A1, catalog(), Set.of());
        assertEquals(UUID.fromString("33f8d7fc-6a86-3f43-92f6-8e2864d279a9"), question.vocabularyId());
        assertEquals("A1 nghĩa 16", question.correctAnswer());
        assertEquals(List.of("A1 nghĩa 16", "A1 nghĩa 25", "A1 nghĩa 17", "A1 nghĩa 1"), question.options());
    }

    @Test
    void snapshotOrderAndExactDuplicateRowsDoNotChangeQuestionOrOptionOrder() {
        List<AssessmentVocabulary> catalog = catalog();
        AssessmentQuestion expected = selector.select(ASSESSMENT, 17, CefrDifficulty.B1, catalog, Set.of());
        Collections.reverse(catalog);
        catalog.add(catalog.getFirst());
        assertEquals(expected, selector.select(ASSESSMENT, 17, CefrDifficulty.B1, catalog, Set.of()));
        assertEquals(expected, new AssessmentQuestionSelector(AssessmentBlockCalculatorTests.properties())
                .select(ASSESSMENT, 17, CefrDifficulty.B1, catalog, Set.of()));
    }

    @Test
    void targetsNeverRepeatAndExhaustionDoesNotFallbackToAnotherLevel() {
        List<AssessmentVocabulary> catalog = catalog();
        Set<UUID> used = new HashSet<>();
        for (int sequence = 1; sequence <= 30; sequence++) {
            AssessmentQuestion question = selector.select(ASSESSMENT, sequence, CefrDifficulty.A1, catalog, used);
            assertTrue(used.add(question.vocabularyId()));
        }
        assertEquals(30, used.size());
        assertThrows(AssessmentContentUnavailableException.class,
                () -> selector.select(ASSESSMENT, 31, CefrDifficulty.A1, catalog, used));
    }

    @Test
    void distractorsMayReusePreviouslyAskedTargets() {
        List<AssessmentVocabulary> catalog = catalog();
        Set<UUID> used = catalog.stream().filter(word -> word.difficulty() == CefrDifficulty.A1)
                .map(AssessmentVocabulary::id).collect(Collectors.toSet());
        UUID onlyRemaining = used.iterator().next();
        used.remove(onlyRemaining);
        AssessmentQuestion question = selector.select(ASSESSMENT, 30, CefrDifficulty.A1, catalog, used);
        assertEquals(onlyRemaining, question.vocabularyId());
        assertTrue(question.options().stream().allMatch(option -> option.startsWith("A1 nghĩa ")));
    }

    @Test
    void nearestLevelFallbackPrefersLowerLevelOnEqualDistance() {
        List<AssessmentVocabulary> catalog = catalog().stream().map(word ->
                word.difficulty() == CefrDifficulty.A2 ? withMeaning(word, "đáp án") : word).toList();
        AssessmentQuestion question = selector.select(ASSESSMENT, 1, CefrDifficulty.A2, catalog, Set.of());
        assertEquals("đáp án", question.correctAnswer());
        assertEquals(3, question.options().stream().filter(option -> option.startsWith("A1 nghĩa ")).count());
    }

    @Test
    void fallbackSkipsDuplicateMeaningsAndContinuesToNextLevel() {
        List<AssessmentVocabulary> catalog = catalog().stream().map(word -> switch (word.difficulty()) {
            case A1 -> withMeaning(word, "khác 1");
            case A2 -> withMeaning(word, "đáp án");
            case B1 -> withMeaning(word, "khác 2");
            case B2 -> withMeaning(word, "khác 3");
            default -> word;
        }).toList();
        AssessmentQuestion question = selector.select(ASSESSMENT, 1, CefrDifficulty.A2, catalog, Set.of());
        assertEquals(Set.of("đáp án", "khác 1", "khác 2", "khác 3"), Set.copyOf(question.options()));
    }

    @Test
    void meaningEqualityIsExactAndRawWordAndMeaningsArePreserved() {
        List<AssessmentVocabulary> catalog = catalog().stream().map(word -> {
            String meaning = switch (word.difficulty()) {
                case A1 -> "nghĩa";
                case A2 -> " nghĩa ";
                case B1 -> "NGHĨA";
                default -> "khác";
            };
            return new AssessmentVocabulary(word.id(), word.difficulty(), " raw ", word.partOfSpeech(), meaning, true);
        }).toList();
        AssessmentQuestion question = selector.select(ASSESSMENT, 1, CefrDifficulty.A1, catalog, Set.of());
        assertEquals("Từ ' raw ' có nghĩa là gì?", question.questionText());
        assertEquals(Set.of("nghĩa", " nghĩa ", "NGHĨA", "khác"), Set.copyOf(question.options()));
    }

    @ParameterizedTest
    @EnumSource(CefrDifficulty.class)
    void everyLevelMustHaveThirtyUsableDistinctIdsEvenWhenNotTargetLevel(CefrDifficulty level) {
        List<AssessmentVocabulary> catalog = catalog();
        AssessmentVocabulary removed = catalog.stream().filter(word -> word.difficulty() == level).findFirst().orElseThrow();
        catalog.remove(removed);
        catalog.add(catalog.getFirst());
        assertThrows(AssessmentContentUnavailableException.class,
                () -> selector.select(ASSESSMENT, 1, CefrDifficulty.A1, catalog, Set.of()));
    }

    @Test
    void unusableInactiveAndBlankRowsCannotSatisfyReadiness() {
        for (int variant = 0; variant < 5; variant++) {
            List<AssessmentVocabulary> catalog = catalog();
            AssessmentVocabulary original = catalog.removeFirst();
            AssessmentVocabulary invalid = new AssessmentVocabulary(original.id(), original.difficulty(),
                    variant == 1 ? " \t" : original.word(), variant == 2 ? "" : original.partOfSpeech(),
                    variant == 3 ? "\n" : variant == 4 ? null : original.meaningVi(), variant != 0);
            catalog.add(invalid);
            assertThrows(AssessmentContentUnavailableException.class,
                    () -> selector.select(ASSESSMENT, 1, CefrDifficulty.A1, catalog, Set.of()));
        }
    }

    @Test
    void thirtyRecordsDoNotRequireThirtyDistinctMeaningsButFourOptionsRemainMandatory() {
        List<AssessmentVocabulary> catalog = catalog().stream().map(word -> withMeaning(word, "một nghĩa")).toList();
        AssessmentContentUnavailableException error = assertThrows(AssessmentContentUnavailableException.class,
                () -> selector.select(ASSESSMENT, 1, CefrDifficulty.A1, catalog, Set.of()));
        assertEquals("ASSESSMENT_CONTENT_UNAVAILABLE", AssessmentContentUnavailableException.CODE);
        assertFalse(error.getMessage().isBlank());
    }

    @Test
    void rejectsContradictoryDuplicateIdsAndInvalidSequence() {
        List<AssessmentVocabulary> catalog = catalog();
        catalog.add(withMeaning(catalog.getFirst(), "mâu thuẫn"));
        assertThrows(IllegalArgumentException.class,
                () -> selector.select(ASSESSMENT, 1, CefrDifficulty.A1, catalog, Set.of()));
        for (int sequence : new int[] {0, 51}) {
            assertThrows(IllegalArgumentException.class,
                    () -> selector.select(ASSESSMENT, sequence, CefrDifficulty.A1, catalog(), Set.of()));
        }
    }

    private AssessmentVocabulary withMeaning(AssessmentVocabulary word, String meaning) {
        return new AssessmentVocabulary(word.id(), word.difficulty(), word.word(), word.partOfSpeech(), meaning, word.active());
    }

    private List<AssessmentVocabulary> catalog() {
        List<AssessmentVocabulary> words = new ArrayList<>();
        for (CefrDifficulty level : CefrDifficulty.values()) {
            for (int index = 0; index < 30; index++) {
                UUID id = UUID.nameUUIDFromBytes((level + ":" + index).getBytes(StandardCharsets.UTF_8));
                words.add(new AssessmentVocabulary(id, level, "word" + index, "noun", level + " nghĩa " + index, true));
            }
        }
        return words;
    }
}
