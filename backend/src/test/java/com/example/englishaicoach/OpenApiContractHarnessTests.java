package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.englishaicoach.common.exception.ApiErrorResponse;
import com.example.englishaicoach.learning.dto.SubmitLearningAttemptRequest;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class OpenApiContractHarnessTests extends OpenApiContractTestSupport {

    private static final Set<String> CANONICAL_REUSABLE_ERROR_RESPONSES = Set.of(
            "ValidationError",
            "Unauthorized",
            "Forbidden",
            "NotFound",
            "Conflict",
            "RateLimited");

    private static SwaggerParseResult parseResult;
    private static OpenAPI openApi;
    private static AnnotationConfigWebApplicationContext runtimeContext;
    private static MockMvc mockMvc;

    @BeforeAll
    static void loadCanonicalOpenApi() throws IOException {
        parseResult = parseCanonicalOpenApi();
        openApi = requireOpenApi(parseResult);
        runtimeContext = runtimeContext(CanonicalLearningAttemptController.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(runtimeContext).build();
    }

    @AfterAll
    static void closeRuntimeContext() {
        runtimeContext.close();
    }

    @Test
    void parsesAndValidatesCanonicalOpenApi31WithoutMessages() {
        assertEquals(List.of(), parseResult.getMessages());
        assertEquals("3.1.0", openApi.getOpenapi());
        assertEquals("1.4.0", openApi.getInfo().getVersion());
    }

    @Test
    void preservesCanonicalPathAndOperationInventory() {
        List<Operation> operations = openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .toList();
        Set<String> operationIds = new HashSet<>();

        assertEquals(72, openApi.getPaths().size());
        assertEquals(76, operations.size());
        assertTrue(operations.stream().allMatch(operation -> operation.getOperationId() != null));
        assertTrue(operations.stream()
                .map(Operation::getOperationId)
                .allMatch(operationIds::add), "operationId phải duy nhất");
    }

    @Test
    void alignsLearningAttemptDtoOperationAndStatusesWithOpenApi() {
        assertRecordMatchesSchema(openApi, "SubmitLearningAttemptRequest", SubmitLearningAttemptRequest.class);
        Schema<?> requestSchema = requireSchema(openApi, "SubmitLearningAttemptRequest");
        Set<String> required = new HashSet<>(requestSchema.getRequired());

        assertEquals(requestSchema.getProperties().keySet(), required);
        assertEquals(Boolean.FALSE, requestSchema.getAdditionalProperties());
        assertFalse(requestSchema.getProperties().containsKey("isCorrect"));
        assertEquals(BigDecimal.ZERO, requestSchema.getProperties().get("responseTimeMs").getMinimum());
        assertEquals(BigDecimal.ZERO, requestSchema.getProperties().get("answerQuality").getMinimum());
        assertEquals(BigDecimal.valueOf(5), requestSchema.getProperties().get("answerQuality").getMaximum());

        Operation operation = requireOperation(openApi, "/learning/attempts", PathItem.HttpMethod.POST);
        assertEquals("submitLearningAttempt", operation.getOperationId());
        assertNotNull(operation.getRequestBody());
        assertEquals(
                "#/components/schemas/SubmitLearningAttemptRequest",
                operation.getRequestBody().getContent().get("application/json").getSchema().get$ref());
        assertEquals(Set.of("200", "400", "409", "429"), responseStatuses(operation));
        assertOperationResponsesResolveToSchema(
                openApi,
                operation,
                Set.of("400", "409", "429"),
                "ErrorResponse");
    }

    @Test
    void alignsRegisteredSpringMvcHandlerWithCanonicalOpenApiOperation() throws Exception {
        RequestMappingHandlerMapping handlerMapping =
                runtimeContext.getBean(RequestMappingHandlerMapping.class);
        RegisteredHandler registeredHandler = requireRegisteredHandler(
                handlerMapping,
                CanonicalLearningAttemptController.class,
                "submitLearningAttempt");

        assertRuntimeHandlerMatchesOpenApi(
                openApi,
                registeredHandler,
                "/learning/attempts",
                PathItem.HttpMethod.POST);

        mockMvc.perform(post("/api/v1/learning/attempts")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "11111111-1111-1111-1111-111111111111",
                                  "vocabularyId": "22222222-2222-2222-2222-222222222222",
                                  "attemptType": "WORD_RECALL",
                                  "responseTimeMs": 2500,
                                  "answerQuality": 4,
                                  "eventId": "33333333-3333-3333-3333-333333333333"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void detectsRuntimeControllerHttpMethodDrift() {
        try (AnnotationConfigWebApplicationContext driftContext =
                     runtimeContext(DriftedLearningAttemptController.class)) {
            RequestMappingHandlerMapping handlerMapping =
                    driftContext.getBean(RequestMappingHandlerMapping.class);
            RegisteredHandler registeredHandler = requireRegisteredHandler(
                    handlerMapping,
                    DriftedLearningAttemptController.class,
                    "submitLearningAttempt");

            AssertionFailedError failure = assertThrows(
                    AssertionFailedError.class,
                    () -> assertRuntimeHandlerMatchesOpenApi(
                            openApi,
                            registeredHandler,
                            "/learning/attempts",
                            PathItem.HttpMethod.POST));

            assertTrue(failure.getMessage().contains("Runtime HTTP method"));
        }
    }

    @Test
    void detectsRuntimeControllerRequestBodyDrift() {
        try (AnnotationConfigWebApplicationContext driftContext =
                     runtimeContext(DriftedRequestLearningAttemptController.class)) {
            RegisteredHandler registeredHandler = requireRegisteredHandler(
                    driftContext.getBean(RequestMappingHandlerMapping.class),
                    DriftedRequestLearningAttemptController.class,
                    "submitLearningAttempt");

            AssertionFailedError failure = assertThrows(
                    AssertionFailedError.class,
                    () -> assertRuntimeHandlerMatchesOpenApi(
                            openApi,
                            registeredHandler,
                            "/learning/attempts",
                            PathItem.HttpMethod.POST));

            assertTrue(failure.getMessage().contains("Runtime request-body Java type"));
        }
    }

    @Test
    void detectsRuntimeControllerResponseBodyDrift() {
        try (AnnotationConfigWebApplicationContext driftContext =
                     runtimeContext(DriftedResponseLearningAttemptController.class)) {
            RegisteredHandler registeredHandler = requireRegisteredHandler(
                    driftContext.getBean(RequestMappingHandlerMapping.class),
                    DriftedResponseLearningAttemptController.class,
                    "submitLearningAttempt");

            AssertionFailedError failure = assertThrows(
                    AssertionFailedError.class,
                    () -> assertRuntimeHandlerMatchesOpenApi(
                            openApi,
                            registeredHandler,
                            "/learning/attempts",
                            PathItem.HttpMethod.POST));

            assertTrue(failure.getMessage().contains("Runtime response-body Java type"));
        }
    }

    @Test
    void detectsOperationErrorSchemaReferenceDrift() {
        Operation driftedOperation = new Operation().responses(new ApiResponses().addApiResponse(
                "400",
                new ApiResponse().content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(
                                new Schema<>().$ref("#/components/schemas/LearningAttemptResponse"))))));

        AssertionFailedError failure = assertThrows(
                AssertionFailedError.class,
                () -> assertOperationResponsesResolveToSchema(
                        openApi,
                        driftedOperation,
                        Set.of("400"),
                        "ErrorResponse"));

        assertTrue(failure.getMessage().contains("OpenAPI response 400"));
    }

    @Test
    void allCanonicalReusableErrorResponsesResolveToErrorResponse() {
        assertEquals(
                CANONICAL_REUSABLE_ERROR_RESPONSES,
                openApi.getComponents().getResponses().keySet());
        CANONICAL_REUSABLE_ERROR_RESPONSES.forEach(responseName ->
                assertReusableResponseResolvesToSchema(openApi, responseName, "ErrorResponse"));
    }

    @Test
    void detectsReusableErrorResponseSchemaDrift() {
        OpenAPI driftedOpenApi = new OpenAPI().components(new Components()
                .addSchemas("ErrorResponse", new Schema<>().type("object"))
                .addSchemas("LearningAttemptResponse", new Schema<>().type("object"))
                .addResponses(
                        "Unauthorized",
                        new ApiResponse().content(new Content().addMediaType(
                                "application/json",
                                new MediaType().schema(new Schema<>().$ref(
                                        "#/components/schemas/LearningAttemptResponse"))))));

        AssertionFailedError failure = assertThrows(
                AssertionFailedError.class,
                () -> assertReusableResponseResolvesToSchema(
                        driftedOpenApi,
                        "Unauthorized",
                        "ErrorResponse"));

        assertTrue(failure.getMessage().contains("Reusable response Unauthorized"));
    }

    @Test
    void alignsRuntimeErrorEnvelopeAndReusableErrorResponsesWithOpenApi() {
        assertRecordMatchesSchema(openApi, "ErrorResponse", ApiErrorResponse.class);
        assertRecordMatchesSchema(openApi, "LearningAttemptResponse", LearningAttemptResponse.class);
        assertRecordMatchesSchema(openApi, "SrsResult", SrsResult.class);
        assertRecordMatchesSchema(openApi, "VocabularyProgressResponse", VocabularyProgressResponse.class);
        Schema<?> errorSchema = requireSchema(openApi, "ErrorResponse");

        assertEquals(
                Set.of("timestamp", "status", "code", "message", "path"),
                new HashSet<>(errorSchema.getRequired()));
        assertEquals("date-time", errorSchema.getProperties().get("timestamp").getFormat());
        assertSchemaType(errorSchema.getProperties().get("status"), "integer");
        assertSchemaType(errorSchema.getProperties().get("details"), "array");
        assertEquals(
                CANONICAL_REUSABLE_ERROR_RESPONSES,
                openApi.getComponents().getResponses().keySet());
    }

    private static AnnotationConfigWebApplicationContext runtimeContext(Class<?> controllerType) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebMvcContractTestConfiguration.class, controllerType);
        context.refresh();
        return context;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class WebMvcContractTestConfiguration {
    }

    @RestController
    @RequestMapping("/api/v1")
    static class CanonicalLearningAttemptController {

        @PostMapping(
                value = "/learning/attempts",
                consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.OK)
        ResponseEntity<LearningAttemptResponse> submitLearningAttempt(
                @RequestBody SubmitLearningAttemptRequest request) {
            LearningAttemptResponse response = new LearningAttemptResponse(
                    UUID.fromString("44444444-4444-4444-4444-444444444444"),
                    true,
                    request.answerQuality(),
                    new SrsResult(2.5, 2.5, 6, 17, 2, 3, Instant.parse("2026-09-14T10:30:00Z"), "sm2-ext-v1"),
                    new VocabularyProgressResponse(
                            request.vocabularyId(), "REVIEWING", 2.5, 17, 3,
                            Instant.parse("2026-09-14T10:30:00Z"), 8, 2));
            return ResponseEntity.ok(response);
        }
    }

    @RestController
    @RequestMapping("/api/v1")
    static class DriftedLearningAttemptController {

        @PutMapping(
                value = "/learning/attempts",
                consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.OK)
        ResponseEntity<LearningAttemptResponse> submitLearningAttempt(
                @RequestBody SubmitLearningAttemptRequest request) {
            return ResponseEntity.ok(null);
        }
    }

    @RestController
    @RequestMapping("/api/v1")
    static class DriftedRequestLearningAttemptController {

        @PostMapping(
                value = "/learning/attempts",
                consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.OK)
        ResponseEntity<LearningAttemptResponse> submitLearningAttempt(
                @RequestBody DriftedLearningAttemptRequest request) {
            return ResponseEntity.ok(null);
        }
    }

    @RestController
    @RequestMapping("/api/v1")
    static class DriftedResponseLearningAttemptController {

        @PostMapping(
                value = "/learning/attempts",
                consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(HttpStatus.OK)
        ResponseEntity<DriftedLearningAttemptResponse> submitLearningAttempt(
                @RequestBody SubmitLearningAttemptRequest request) {
            return ResponseEntity.ok(new DriftedLearningAttemptResponse(request.eventId()));
        }
    }

    record LearningAttemptResponse(
            UUID attemptId,
            boolean isCorrect,
            Integer answerQuality,
            SrsResult srs,
            VocabularyProgressResponse progress) {
    }

    record SrsResult(
            double oldEaseFactor,
            double newEaseFactor,
            int oldIntervalDays,
            int newIntervalDays,
            int oldRepetitions,
            int newRepetitions,
            Instant nextReviewAt,
            String algorithmVersion) {
    }

    record VocabularyProgressResponse(
            UUID vocabularyId,
            String status,
            double easeFactor,
            int intervalDays,
            int repetitions,
            Instant nextReviewAt,
            int correctCount,
            int incorrectCount) {
    }

    record DriftedLearningAttemptRequest(UUID eventId) {
    }

    record DriftedLearningAttemptResponse(UUID eventId) {
    }
}
