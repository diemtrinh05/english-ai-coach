package com.example.englishaicoach;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.GlobalExceptionHandler;
import com.example.englishaicoach.common.mapper.EntityResponseMapper;
import com.example.englishaicoach.common.pagination.PaginationConvention;
import com.example.englishaicoach.common.response.PaginatedResponse;
import com.example.englishaicoach.common.validation.PaginationRequest;
import com.example.englishaicoach.learning.AttemptType;
import com.example.englishaicoach.learning.dto.SubmitLearningAttemptRequest;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ApiConventionTests {

    private static final UUID SESSION_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID VOCABULARY_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID EVENT_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ConventionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void appliesCanonicalPaginationDefaultsAndBounds() {
        PaginationRequest defaults = new PaginationRequest(null, null);

        assertEquals(PaginationConvention.DEFAULT_PAGE, defaults.page());
        assertEquals(PaginationConvention.DEFAULT_SIZE, defaults.size());
        assertTrue(validator.validate(defaults).isEmpty());
        assertTrue(validator.validate(new PaginationRequest(0, 1)).isEmpty());
        assertTrue(validator.validate(new PaginationRequest(0, 100)).isEmpty());
        assertFalse(validator.validate(new PaginationRequest(-1, 20)).isEmpty());
        assertFalse(validator.validate(new PaginationRequest(0, 0)).isEmpty());
        assertFalse(validator.validate(new PaginationRequest(0, 101)).isEmpty());
    }

    @Test
    void convertsUnpagedEmptyPageUsingCanonicalDefaults() {
        PaginatedResponse<String> response = PaginatedResponse.from(Page.empty());

        assertTrue(response.content().isEmpty());
        assertEquals(PaginationConvention.DEFAULT_PAGE, response.page());
        assertEquals(PaginationConvention.DEFAULT_SIZE, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertFalse(response.hasNext());
    }

    @Test
    void keepsPaginatedResponseSizeInvariant() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaginatedResponse<>(List.of(), 0, 0, 0, 0, false));
    }

    @Test
    void convertsEmptyPageWhilePreservingExplicitPageable() {
        PaginatedResponse<String> response = PaginatedResponse.from(
                Page.empty(PageRequest.of(2, 50)));

        assertTrue(response.content().isEmpty());
        assertEquals(2, response.page());
        assertEquals(50, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertFalse(response.hasNext());
    }

    @Test
    void mapperConvertsEmptyPagesWithoutInvokingEntityMapping() {
        EntityResponseMapper<TestEntity, TestResponse> mapper = entity -> {
            throw new AssertionError("Mapper không được gọi khi Page không có entity");
        };

        PaginatedResponse<TestResponse> defaults = mapper.toResponsePage(Page.empty());
        PaginatedResponse<TestResponse> explicit = mapper.toResponsePage(
                Page.empty(PageRequest.of(2, 50)));

        assertTrue(defaults.content().isEmpty());
        assertEquals(PaginationConvention.DEFAULT_PAGE, defaults.page());
        assertEquals(PaginationConvention.DEFAULT_SIZE, defaults.size());
        assertEquals(0, defaults.totalElements());
        assertEquals(0, defaults.totalPages());
        assertFalse(defaults.hasNext());
        assertTrue(explicit.content().isEmpty());
        assertEquals(2, explicit.page());
        assertEquals(50, explicit.size());
        assertEquals(0, explicit.totalElements());
        assertEquals(0, explicit.totalPages());
        assertFalse(explicit.hasNext());
    }

    @Test
    void returnsCanonicalPaginatedResponseThroughMapper() throws Exception {
        mockMvc.perform(get("/test/conventions/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].displayName").value("alpha"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.items").doesNotExist());
    }

    @Test
    void mapperReturnsResponseDtosAndNeverTheSourceEntity() {
        TestEntity entity = new TestEntity("alpha");
        List<TestResponse> responses = ConventionController.MAPPER.toResponseList(List.of(entity));

        assertEquals(List.of(new TestResponse("alpha")), responses);
        assertEquals(TestResponse.class, responses.getFirst().getClass());
        assertThrows(UnsupportedOperationException.class, () -> responses.add(new TestResponse("beta")));
    }

    @Test
    void learningAttemptRequestContainsOnlyCanonicalClientFields() {
        Set<String> components = Arrays.stream(SubmitLearningAttemptRequest.class.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "sessionId", "vocabularyId", "attemptType",
                "responseTimeMs", "answerQuality", "eventId"), components);
        assertFalse(components.contains("isCorrect"));
        assertEquals(Set.of(
                        "FLASHCARD", "WORD_RECALL", "WORD_MEANING",
                        "MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING"),
                Arrays.stream(AttemptType.values()).map(Enum::name).collect(Collectors.toSet()));
    }

    @Test
    void acceptsCanonicalLearningAttemptBoundaries() throws Exception {
        mockMvc.perform(post("/test/conventions/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(0, 0, "")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/test/conventions/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(5, 2500, "")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidLearningAttemptValuesWithCanonicalEnvelope() throws Exception {
        mockMvc.perform(post("/test/conventions/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(6, -1, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.message").value("Yêu cầu không hợp lệ."))
                .andExpect(jsonPath("$.path").value("/test/conventions/attempt"))
                .andExpect(jsonPath("$.details", hasSize(2)));
    }

    @Test
    void rejectsMissingLearningAttemptFieldsWithCanonicalValidationDetails() throws Exception {
        mockMvc.perform(post("/test/conventions/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.details", hasSize(6)));
    }

    @Test
    void rejectsClientSuppliedIsCorrect() throws Exception {
        mockMvc.perform(post("/test/conventions/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(4, 2500, ",\"isCorrect\":true")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR));
    }

    private String requestJson(int answerQuality, int responseTimeMs, String additionalProperty) {
        return """
                {
                  "sessionId": "%s",
                  "vocabularyId": "%s",
                  "attemptType": "WORD_RECALL",
                  "responseTimeMs": %d,
                  "answerQuality": %d,
                  "eventId": "%s"%s
                }
                """.formatted(
                SESSION_ID, VOCABULARY_ID, responseTimeMs, answerQuality, EVENT_ID, additionalProperty);
    }

    @RestController
    private static final class ConventionController {

        private static final EntityResponseMapper<TestEntity, TestResponse> MAPPER =
                entity -> new TestResponse(entity.name());

        @GetMapping("/test/conventions/page")
        PaginatedResponse<TestResponse> page() {
            var entities = new PageImpl<>(
                    List.of(new TestEntity("alpha"), new TestEntity("beta")),
                    PageRequest.of(0, 2),
                    3);
            return MAPPER.toResponsePage(entities);
        }

        @PostMapping(value = "/test/conventions/attempt", consumes = MediaType.APPLICATION_JSON_VALUE)
        void attempt(@Valid @RequestBody SubmitLearningAttemptRequest request) {
        }
    }

    private record TestEntity(String name) {
    }

    private record TestResponse(String displayName) {
    }
}
