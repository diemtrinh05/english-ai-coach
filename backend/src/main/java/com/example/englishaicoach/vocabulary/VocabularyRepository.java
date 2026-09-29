package com.example.englishaicoach.vocabulary;

import com.example.englishaicoach.vocabulary.dto.TopicResponse;
import com.example.englishaicoach.vocabulary.dto.VocabularyExampleResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VocabularyRepository {

    private static final String FILTER = """
            FROM vocabulary v
            JOIN cefr_levels c ON c.id = v.cefr_level_id
            WHERE v.is_active = TRUE
              AND (CAST(:search AS text) IS NULL OR POSITION(LOWER(:search) IN LOWER(v.word)) > 0
                   OR POSITION(LOWER(:search) IN LOWER(COALESCE(v.meaning_vi, ''))) > 0
                   OR POSITION(LOWER(:search) IN LOWER(COALESCE(v.meaning_en, ''))) > 0)
              AND (CAST(:cefr AS text) IS NULL OR c.code = :cefr)
              AND (CAST(:partOfSpeech AS text) IS NULL OR LOWER(v.part_of_speech) = LOWER(:partOfSpeech))
              AND (CAST(:topicId AS uuid) IS NULL OR EXISTS (
                  SELECT 1 FROM vocabulary_topics vt
                  JOIN topics t ON t.id = vt.topic_id AND t.is_active = TRUE
                  WHERE vt.vocabulary_id = v.id AND vt.topic_id = :topicId))
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public VocabularyRepository(JdbcTemplate jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public long countActive(String search, String cefr, UUID topicId, String partOfSpeech) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) " + FILTER,
                filters(search, cefr, topicId, partOfSpeech), Long.class);
        return count == null ? 0 : count;
    }

    public List<Vocabulary> findActive(String search, String cefr, UUID topicId,
            String partOfSpeech, String orderBy, int limit, long offset) {
        MapSqlParameterSource parameters = filters(search, cefr, topicId, partOfSpeech)
                .addValue("limit", limit).addValue("offset", offset);
        // orderBy chỉ được VocabularyService chọn từ whitelist hằng số.
        return jdbc.query("""
                SELECT v.id, v.word, v.phonetic_ipa, v.meaning_vi, v.meaning_en,
                       v.part_of_speech, c.code AS cefr, v.audio_url, v.image_url
                """ + FILTER + " ORDER BY " + orderBy + ", v.id LIMIT :limit OFFSET :offset",
                parameters, (rs, row) -> mapVocabulary(rs));
    }

    public Optional<Vocabulary> findActiveById(UUID id) {
        return jdbc.query("""
                SELECT v.id, v.word, v.phonetic_ipa, v.meaning_vi, v.meaning_en,
                       v.part_of_speech, c.code AS cefr, v.audio_url, v.image_url
                FROM vocabulary v JOIN cefr_levels c ON c.id = v.cefr_level_id
                WHERE v.id = :id AND v.is_active = TRUE
                """, new MapSqlParameterSource("id", id),
                (rs, row) -> mapVocabulary(rs)).stream().findFirst();
    }

    public Map<UUID, List<TopicResponse>> findTopics(List<UUID> vocabularyIds) {
        Map<UUID, List<TopicResponse>> result = new HashMap<>();
        if (vocabularyIds.isEmpty()) {
            return result;
        }
        jdbc.query("""
                SELECT vt.vocabulary_id, t.id, t.name, t.description, t.icon_url,
                       t.parent_topic_id, t.is_active
                FROM vocabulary_topics vt
                JOIN topics t ON t.id = vt.topic_id
                WHERE vt.vocabulary_id IN (:ids) AND t.is_active = TRUE
                ORDER BY LOWER(t.name), t.id
                """, new MapSqlParameterSource("ids", vocabularyIds), rs -> {
                    UUID vocabularyId = rs.getObject("vocabulary_id", UUID.class);
                    TopicResponse topic = new TopicResponse(rs.getObject("id", UUID.class),
                            rs.getString("name"), rs.getString("description"),
                            rs.getString("icon_url"), rs.getObject("parent_topic_id", UUID.class),
                            rs.getBoolean("is_active"));
                    result.computeIfAbsent(vocabularyId, ignored -> new ArrayList<>()).add(topic);
                });
        return result;
    }

    public Map<UUID, List<VocabularyExampleResponse>> findExamples(List<UUID> vocabularyIds) {
        Map<UUID, List<VocabularyExampleResponse>> result = new HashMap<>();
        if (vocabularyIds.isEmpty()) {
            return result;
        }
        jdbc.query("""
                SELECT vocabulary_id, id, example_text, translation_text, source
                FROM vocabulary_examples
                WHERE vocabulary_id IN (:ids)
                ORDER BY created_at, id
                """, new MapSqlParameterSource("ids", vocabularyIds), rs -> {
                    UUID vocabularyId = rs.getObject("vocabulary_id", UUID.class);
                    VocabularyExampleResponse example = new VocabularyExampleResponse(
                            rs.getObject("id", UUID.class), rs.getString("example_text"),
                            rs.getString("translation_text"), rs.getString("source"));
                    result.computeIfAbsent(vocabularyId, ignored -> new ArrayList<>()).add(example);
                });
        return result;
    }

    private MapSqlParameterSource filters(String search, String cefr, UUID topicId, String partOfSpeech) {
        return new MapSqlParameterSource().addValue("search", search).addValue("cefr", cefr)
                .addValue("topicId", topicId).addValue("partOfSpeech", partOfSpeech);
    }

    private Vocabulary mapVocabulary(ResultSet rs) throws SQLException {
        return new Vocabulary(rs.getObject("id", UUID.class), rs.getString("word"),
                rs.getString("phonetic_ipa"), rs.getString("meaning_vi"),
                rs.getString("meaning_en"), rs.getString("part_of_speech"),
                rs.getString("cefr"), rs.getString("audio_url"), rs.getString("image_url"));
    }
}
