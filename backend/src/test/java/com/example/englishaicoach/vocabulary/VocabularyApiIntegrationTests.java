package com.example.englishaicoach.vocabulary;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class VocabularyApiIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final UUID FOOD_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    @Test
    void curatedMigrationCoversEightTopicsAndCanBeReappliedWithoutDuplicates() {
        Integer mappings = jdbc.queryForObject("SELECT COUNT(*) FROM vocabulary_topics", Integer.class);
        Integer topics = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT topic_id) FROM vocabulary_topics", Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(27, mappings);
        org.junit.jupiter.api.Assertions.assertEquals(8, topics);

        DatabasePopulatorUtils.execute(
                new ResourceDatabasePopulator(new ClassPathResource(
                        "db/migration/V6__seed_demo_vocabulary_topics.sql")), dataSource);
        org.junit.jupiter.api.Assertions.assertEquals(27,
                jdbc.queryForObject("SELECT COUNT(*) FROM vocabulary_topics", Integer.class));
    }

    @Test
    void listsSeedVocabularyWithStablePaginationAndCanonicalFields() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary").param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].word").isNotEmpty())
                .andExpect(jsonPath("$.content[0].cefr").isNotEmpty())
                .andExpect(jsonPath("$.content[0].topics").isArray())
                .andExpect(jsonPath("$.content[0].examples").isArray())
                .andExpect(jsonPath("$.totalElements").value(180))
                .andExpect(jsonPath("$.totalPages").value(60))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void filtersByCefrTopicPartOfSpeechAndLiteralCaseInsensitiveSearch() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary").param("cefr", "A1")
                        .param("topicId", FOOD_ID.toString()).param("partOfSpeech", "NOUN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].word").value("apple"))
                .andExpect(jsonPath("$.content[0].topics[0].name").value("Food"));

        mockMvc.perform(get("/api/v1/vocabulary").param("search", "  APP  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].word").value("apple"));
        mockMvc.perform(get("/api/v1/vocabulary").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void sortsByWhitelistedFieldsAndRejectsInjectedSort() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary").param("sort", "word,desc").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].word").value("write"));
        mockMvc.perform(get("/api/v1/vocabulary").param("sort", "word,asc;DROP TABLE vocabulary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsDetailAndPlainExamplesArrayWithCanonicalExampleFields() throws Exception {
        UUID apple = wordId("apple");
        UUID example = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO vocabulary_examples
                (id, vocabulary_id, example_text, translation_text, source, created_at)
                VALUES (?, ?, ?, ?, 'MANUAL', CURRENT_TIMESTAMP)
                """, example, apple, "An apple is on the table.", "Một quả táo ở trên bàn.");

        mockMvc.perform(get("/api/v1/vocabulary/" + apple))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("apple"))
                .andExpect(jsonPath("$.topics[0].name").value("Food"))
                .andExpect(jsonPath("$.examples[0].id").value(example.toString()))
                .andExpect(jsonPath("$.examples[0].exampleText").value("An apple is on the table."))
                .andExpect(jsonPath("$.examples[0].translationText").value("Một quả táo ở trên bàn."))
                .andExpect(jsonPath("$.examples[0].source").value("MANUAL"));
        mockMvc.perform(get("/api/v1/vocabulary/" + apple + "/examples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(example.toString()));
    }

    @Test
    void excludesInactiveVocabularyAndInactiveTopics() throws Exception {
        UUID apple = wordId("apple");
        jdbc.update("UPDATE vocabulary SET is_active = FALSE WHERE id = ?", apple);
        mockMvc.perform(get("/api/v1/vocabulary").param("search", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/v1/vocabulary/" + apple))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mockMvc.perform(get("/api/v1/vocabulary/" + apple + "/examples"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        UUID bread = wordId("bread");
        jdbc.update("UPDATE topics SET is_active = FALSE WHERE id = ?", FOOD_ID);
        mockMvc.perform(get("/api/v1/vocabulary").param("topicId", FOOD_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/v1/vocabulary/" + bread))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topics", hasSize(0)));
    }

    @Test
    void validatesParametersAndReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/vocabulary").param("size", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/vocabulary").param("cefr", "X9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/vocabulary/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private UUID wordId(String word) {
        return jdbc.queryForObject("SELECT id FROM vocabulary WHERE word = ?", UUID.class, word);
    }
}
