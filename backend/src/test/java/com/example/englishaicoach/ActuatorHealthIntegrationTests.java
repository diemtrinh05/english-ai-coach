package com.example.englishaicoach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.ai.LlmProvider;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class ActuatorHealthIntegrationTests extends PostgreSqlIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthContributorRegistry healthContributorRegistry;

    @MockitoBean
    private LlmProvider llmProvider;

    @Test
    void healthAndReadinessAreUpWhenPostgreSqlIsAvailable() throws Exception {
        when(llmProvider.generate(any())).thenThrow(new IllegalStateException("provider unavailable"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        verifyNoInteractions(llmProvider);
    }

    @Test
    void nonHealthActuatorEndpointsAreNotExposed() throws Exception {
        mockMvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }

    @Test
    void databaseFailureMakesHealthAndReadinessUnavailableButNotLiveness() throws Exception {
        HealthContributor actualDatabase = healthContributorRegistry.unregisterContributor("db");
        try {
            healthContributorRegistry.registerContributor("db", (HealthIndicator) () -> Health.down().build());

            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("DOWN"))
                    .andExpect(jsonPath("$.components").doesNotExist());
            mockMvc.perform(get("/actuator/health/readiness"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("DOWN"));
            mockMvc.perform(get("/actuator/health/liveness"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        } finally {
            healthContributorRegistry.unregisterContributor("db");
            healthContributorRegistry.registerContributor("db", actualDatabase);
        }
    }
}
