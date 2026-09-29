package com.example.englishaicoach.vocabulary;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class TopicApiIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private static final UUID TRAVEL_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsEightCanonicalActiveTopicsWithStablePaginationAndExactFields() throws Exception {
        mockMvc.perform(get("/api/v1/topics").param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").value("Business"))
                .andExpect(jsonPath("$.content[0].isActive").value(true))
                .andExpect(jsonPath("$.content[0].description").value((Object) null))
                .andExpect(jsonPath("$.content[0].iconUrl").value((Object) null))
                .andExpect(jsonPath("$.content[0].parentTopicId").value((Object) null))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(true));

        mockMvc.perform(get("/api/v1/topics").param("page", "2").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Technology"))
                .andExpect(jsonPath("$.content[1].name").value("Travel"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void searchesNamesCaseInsensitivelyAndTreatsWildcardsLiterally() throws Exception {
        mockMvc.perform(get("/api/v1/topics").param("search", "  TRAV  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Travel"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/topics").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void filtersByParentTopicIdAndExcludesInactiveContent() throws Exception {
        UUID childId = UUID.randomUUID();
        UUID inactiveId = UUID.randomUUID();
        insertTopic(childId, "Travel Planning", TRAVEL_ID, true);
        insertTopic(inactiveId, "Travel Archive", TRAVEL_ID, false);

        mockMvc.perform(get("/api/v1/topics").param("parentTopicId", TRAVEL_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(childId.toString()))
                .andExpect(jsonPath("$.content[0].parentTopicId").value(TRAVEL_ID.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/topics/" + inactiveId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void returnsDetailAndNotFoundWithCanonicalError() throws Exception {
        mockMvc.perform(get("/api/v1/topics/" + TRAVEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRAVEL_ID.toString()))
                .andExpect(jsonPath("$.name").value("Travel"))
                .andExpect(jsonPath("$.isActive").value(true));

        mockMvc.perform(get("/api/v1/topics/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void rejectsInvalidPaginationAndUuidParameters() throws Exception {
        mockMvc.perform(get("/api/v1/topics").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/topics").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/topics").param("parentTopicId", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private void insertTopic(UUID id, String name, UUID parentTopicId, boolean active) {
        jdbcTemplate.update("""
                INSERT INTO topics (id, name, parent_topic_id, is_active, created_at, updated_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, name, parentTopicId, active);
    }
}
