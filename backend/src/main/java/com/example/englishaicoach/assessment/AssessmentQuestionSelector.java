package com.example.englishaicoach.assessment;

import com.example.englishaicoach.config.AssessmentProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Chọn câu xác định từ snapshot; không DB, HTTP, thời gian ngầm hoặc random toàn cục. */
public final class AssessmentQuestionSelector {
    private final AssessmentProperties properties;

    public AssessmentQuestionSelector(AssessmentProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public AssessmentQuestion select(UUID assessmentId, int sequenceNo, CefrDifficulty difficulty,
            List<AssessmentVocabulary> catalog, Set<UUID> usedVocabularyIds) {
        Objects.requireNonNull(assessmentId, "assessmentId");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(catalog, "catalog");
        Set<UUID> used = Set.copyOf(usedVocabularyIds);
        if (sequenceNo < 1 || sequenceNo > properties.maximumQuestions()) {
            throw new IllegalArgumentException("sequenceNo nằm ngoài phạm vi assessment");
        }
        // UUID là identity duy nhất; snapshot lặp cùng row không được làm tăng readiness.
        Map<UUID, AssessmentVocabulary> unique = new HashMap<>();
        for (AssessmentVocabulary vocabulary : catalog) {
            Objects.requireNonNull(vocabulary, "vocabulary");
            AssessmentVocabulary previous = unique.putIfAbsent(vocabulary.id(), vocabulary);
            if (previous != null && !previous.equals(vocabulary)) {
                throw new IllegalArgumentException("Snapshot chứa cùng UUID với dữ liệu khác nhau");
            }
        }
        List<AssessmentVocabulary> usable = unique.values().stream()
                .filter(AssessmentVocabulary::usable).toList();
        for (CefrDifficulty level : CefrDifficulty.values()) {
            if (usable.stream().filter(word -> word.difficulty() == level).count()
                    < properties.minimumVocabularyPerLevel()) {
                throw new AssessmentContentUnavailableException();
            }
        }
        Comparator<AssessmentVocabulary> targetOrder = ranking(assessmentId, sequenceNo, "target");
        AssessmentVocabulary target = usable.stream()
                .filter(word -> word.difficulty() == difficulty && !used.contains(word.id()))
                .min(targetOrder).orElseThrow(AssessmentContentUnavailableException::new);
        Comparator<AssessmentVocabulary> distractorOrder = Comparator
                .comparingInt((AssessmentVocabulary word) -> Math.abs(word.difficulty().ordinal() - difficulty.ordinal()))
                .thenComparingInt(word -> word.difficulty().ordinal())
                .thenComparing(ranking(assessmentId, sequenceNo, "distractor"));
        List<AssessmentVocabulary> candidates = usable.stream()
                .filter(word -> !word.id().equals(target.id()))
                .sorted(distractorOrder).toList();
        Set<String> meanings = new HashSet<>();
        meanings.add(target.meaningVi());
        List<AssessmentVocabulary> options = new ArrayList<>();
        options.add(target);
        for (AssessmentVocabulary candidate : candidates) {
            if (meanings.add(candidate.meaningVi())) {
                options.add(candidate);
                if (options.size() == properties.optionsPerQuestion()) {
                    break;
                }
            }
        }
        if (options.size() != properties.optionsPerQuestion()) {
            throw new AssessmentContentUnavailableException();
        }
        options.sort(ranking(assessmentId, sequenceNo, "option"));
        return new AssessmentQuestion(target.id(), target.difficulty(),
                "Từ '" + target.word() + "' có nghĩa là gì?",
                options.stream().map(AssessmentVocabulary::meaningVi).toList(), target.meaningVi());
    }

    private Comparator<AssessmentVocabulary> ranking(UUID assessmentId, int sequenceNo, String purpose) {
        return Comparator.comparing((AssessmentVocabulary word) -> hash(assessmentId, sequenceNo, purpose, word.id()))
                .thenComparing(word -> word.id().toString());
    }

    private String hash(UUID assessmentId, int sequenceNo, String purpose, UUID vocabularyId) {
        String identity = properties.algorithmVersion() + ":" + assessmentId + ":" + sequenceNo
                + ":" + purpose + ":" + vocabularyId;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(identity.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Runtime thiếu SHA-256 bắt buộc", exception);
        }
    }
}
