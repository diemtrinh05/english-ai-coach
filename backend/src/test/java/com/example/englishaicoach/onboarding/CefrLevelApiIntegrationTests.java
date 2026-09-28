package com.example.englishaicoach.onboarding;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class CefrLevelApiIntegrationTests extends PostgreSqlIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsCanonicalSeededLevelsInStableOrderWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cefr-levels"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id")
                        .value("10000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$[0].code").value("A1"))
                .andExpect(jsonPath("$[0].name").value("A1"))
                .andExpect(jsonPath("$[0].sortOrder").value(1))
                .andExpect(jsonPath("$[1].code").value("A2"))
                .andExpect(jsonPath("$[1].sortOrder").value(2))
                .andExpect(jsonPath("$[2].code").value("B1"))
                .andExpect(jsonPath("$[2].sortOrder").value(3))
                .andExpect(jsonPath("$[3].code").value("B2"))
                .andExpect(jsonPath("$[3].sortOrder").value(4))
                .andExpect(jsonPath("$[4].code").value("C1"))
                .andExpect(jsonPath("$[4].sortOrder").value(5))
                .andExpect(jsonPath("$[5].code").value("C2"))
                .andExpect(jsonPath("$[5].sortOrder").value(6))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }
}
