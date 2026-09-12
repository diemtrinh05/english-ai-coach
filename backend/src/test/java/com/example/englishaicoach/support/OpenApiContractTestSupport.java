package com.example.englishaicoach.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public abstract class OpenApiContractTestSupport {

    private static final String OPENAPI_DOCUMENT =
            "docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md";
    private static final Pattern YAML_BLOCK =
            Pattern.compile("```yaml\\R(.*?)\\R```", Pattern.DOTALL);

    protected static SwaggerParseResult parseCanonicalOpenApi() throws IOException {
        String markdown = Files.readString(repositoryRoot().resolve(OPENAPI_DOCUMENT));
        Matcher matcher = YAML_BLOCK.matcher(markdown);
        if (!matcher.find()) {
            throw new AssertionError("Không tìm thấy YAML block trong " + OPENAPI_DOCUMENT);
        }
        return new OpenAPIV3Parser().readContents(matcher.group(1));
    }

    protected static OpenAPI requireOpenApi(SwaggerParseResult result) {
        OpenAPI openApi = result.getOpenAPI();
        assertNotNull(openApi, () -> "Không parse được OpenAPI: " + result.getMessages());
        return openApi;
    }

    protected static Operation requireOperation(
            OpenAPI openApi,
            String path,
            PathItem.HttpMethod method) {
        PathItem pathItem = openApi.getPaths().get(path);
        assertNotNull(pathItem, () -> "Thiếu OpenAPI path: " + path);
        Operation operation = pathItem.readOperationsMap().get(method);
        assertNotNull(operation, () -> "Thiếu OpenAPI operation: " + method + " " + path);
        return operation;
    }

    protected static Schema<?> requireSchema(OpenAPI openApi, String schemaName) {
        Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
        assertNotNull(schema, () -> "Thiếu OpenAPI schema: " + schemaName);
        return schema;
    }

    protected static void assertRecordMatchesSchema(
            OpenAPI openApi,
            String schemaName,
            Class<? extends Record> recordType) {
        Schema<?> schema = requireSchema(openApi, schemaName);
        Set<String> recordComponents = Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> schemaProperties = new LinkedHashSet<>(schema.getProperties().keySet());

        assertEquals(schemaProperties, recordComponents,
                () -> recordType.getSimpleName() + " lệch properties của " + schemaName);
    }

    protected static Set<String> responseStatuses(Operation operation) {
        return new LinkedHashSet<>(operation.getResponses().keySet());
    }

    protected static RegisteredHandler requireRegisteredHandler(
            RequestMappingHandlerMapping handlerMapping,
            Class<?> controllerType,
            String methodName) {
        return handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> controllerType.equals(entry.getValue().getBeanType()))
                .filter(entry -> methodName.equals(entry.getValue().getMethod().getName()))
                .map(entry -> new RegisteredHandler(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Không tìm thấy runtime handler " + controllerType.getSimpleName() + "#" + methodName));
    }

    protected static void assertRuntimeHandlerMatchesOpenApi(
            OpenAPI openApi,
            RegisteredHandler registeredHandler,
            String openApiPath,
            PathItem.HttpMethod openApiMethod) {
        Operation operation = requireOperation(openApi, openApiPath, openApiMethod);
        RequestMappingInfo mappingInfo = registeredHandler.mappingInfo();
        HandlerMethod handlerMethod = registeredHandler.handlerMethod();

        assertEquals(
                Set.of(canonicalRuntimePath(openApi, openApiPath)),
                mappingInfo.getPatternValues(),
                "Runtime path phải khớp OpenAPI server base path và operation path");
        assertEquals(
                Set.of(RequestMethod.valueOf(openApiMethod.name())),
                mappingInfo.getMethodsCondition().getMethods(),
                "Runtime HTTP method phải khớp OpenAPI operation");

        Class<?> requestBodyType = Arrays.stream(handlerMethod.getMethodParameters())
                .filter(parameter -> parameter.hasParameterAnnotation(RequestBody.class))
                .map(parameter -> parameter.getParameterType())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Runtime handler thiếu @RequestBody parameter"));
        Schema<?> openApiRequestSchema = operation.getRequestBody()
                .getContent()
                .get("application/json")
                .getSchema();
        assertEquals(
                "#/components/schemas/" + requestBodyType.getSimpleName(),
                openApiRequestSchema.get$ref(),
                "Runtime request-body Java type phải khớp OpenAPI request schema");

        HttpStatus runtimeSuccessStatus = responseStatus(handlerMethod);
        assertTrue(runtimeSuccessStatus.is2xxSuccessful(), "Runtime response status phải là success status");
        assertTrue(
                operation.getResponses().containsKey(Integer.toString(runtimeSuccessStatus.value())),
                () -> "Runtime success status " + runtimeSuccessStatus.value()
                        + " không có trong OpenAPI responses " + operation.getResponses().keySet());

        Class<?> runtimeResponseBodyType = responseBodyType(handlerMethod);
        ApiResponse openApiSuccessResponse = operation.getResponses()
                .get(Integer.toString(runtimeSuccessStatus.value()));
        assertApiResponseResolvesToSchema(
                openApi,
                openApiSuccessResponse,
                runtimeResponseBodyType.getSimpleName(),
                "Runtime response-body Java type phải khớp OpenAPI success response schema");
    }

    protected static void assertOperationResponsesResolveToSchema(
            OpenAPI openApi,
            Operation operation,
            Set<String> statuses,
            String expectedSchemaName) {
        for (String status : statuses) {
            ApiResponse response = operation.getResponses().get(status);
            assertNotNull(response, () -> "OpenAPI operation thiếu response status " + status);
            assertApiResponseResolvesToSchema(
                    openApi,
                    response,
                    expectedSchemaName,
                    "OpenAPI response " + status + " phải resolve tới " + expectedSchemaName);
        }
    }

    protected static void assertReusableResponseResolvesToSchema(
            OpenAPI openApi,
            String responseName,
            String expectedSchemaName) {
        ApiResponse response = openApi.getComponents().getResponses().get(responseName);
        assertNotNull(response, () -> "Thiếu reusable OpenAPI response " + responseName);
        assertApiResponseResolvesToSchema(
                openApi,
                response,
                expectedSchemaName,
                "Reusable response " + responseName + " phải resolve tới " + expectedSchemaName);
    }

    protected static void assertApiResponseResolvesToSchema(
            OpenAPI openApi,
            ApiResponse response,
            String expectedSchemaName) {
        assertApiResponseResolvesToSchema(
                openApi,
                response,
                expectedSchemaName,
                "OpenAPI response phải resolve tới schema canonical");
    }

    protected static void assertSchemaType(Schema<?> schema, String expectedType) {
        Set<String> openApi31Types = schema.getTypes();
        if (openApi31Types != null && !openApi31Types.isEmpty()) {
            assertEquals(Set.of(expectedType), openApi31Types);
            return;
        }
        assertEquals(expectedType, schema.getType());
    }

    private static String canonicalRuntimePath(OpenAPI openApi, String openApiPath) {
        assertNotNull(openApi.getServers(), "OpenAPI phải khai báo server base path");
        assertTrue(!openApi.getServers().isEmpty(), "OpenAPI phải có ít nhất một server");
        String basePath = URI.create(openApi.getServers().getFirst().getUrl()).getPath();
        return normalizePath(basePath + "/" + openApiPath);
    }

    private static String normalizePath(String path) {
        return "/" + Arrays.stream(path.split("/"))
                .filter(segment -> !segment.isBlank())
                .collect(Collectors.joining("/"));
    }

    private static HttpStatus responseStatus(HandlerMethod handlerMethod) {
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), ResponseStatus.class);
        if (responseStatus == null) {
            responseStatus = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), ResponseStatus.class);
        }
        assertNotNull(responseStatus,
                "Runtime handler phải khai báo @ResponseStatus để contract status xác định được");
        return responseStatus.code();
    }

    private static Class<?> responseBodyType(HandlerMethod handlerMethod) {
        Type returnType = handlerMethod.getMethod().getGenericReturnType();
        if (returnType instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() instanceof Class<?> rawType
                && ResponseEntity.class.isAssignableFrom(rawType)) {
            return rawClass(parameterizedType.getActualTypeArguments()[0]);
        }
        return rawClass(returnType);
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> typeClass) {
            return typeClass;
        }
        if (type instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() instanceof Class<?> rawType) {
            return rawType;
        }
        throw new AssertionError("Không xác định được runtime response-body Java type từ " + type);
    }

    private static void assertApiResponseResolvesToSchema(
            OpenAPI openApi,
            ApiResponse response,
            String expectedSchemaName,
            String message) {
        ApiResponse resolvedResponse = resolveResponse(openApi, response);
        assertNotNull(resolvedResponse.getContent(), message + ": thiếu response content");
        assertNotNull(
                resolvedResponse.getContent().get("application/json"),
                message + ": thiếu application/json content");
        Schema<?> responseSchema = resolvedResponse.getContent().get("application/json").getSchema();
        assertNotNull(responseSchema, message + ": thiếu response schema");
        assertEquals(expectedSchemaName, resolveSchemaName(openApi, responseSchema), message);
    }

    private static ApiResponse resolveResponse(OpenAPI openApi, ApiResponse response) {
        assertNotNull(response, "OpenAPI response không được null");
        ApiResponse resolved = response;
        Set<String> visitedRefs = new HashSet<>();
        while (resolved.get$ref() != null) {
            String responseName = componentName(resolved.get$ref(), "responses");
            assertTrue(visitedRefs.add(responseName), () -> "Chuỗi response $ref bị lặp tại " + responseName);
            resolved = openApi.getComponents().getResponses().get(responseName);
            assertNotNull(resolved, () -> "Không resolve được reusable response " + responseName);
        }
        return resolved;
    }

    private static String resolveSchemaName(OpenAPI openApi, Schema<?> schema) {
        Schema<?> resolved = schema;
        String resolvedName = null;
        Set<String> visitedRefs = new HashSet<>();
        while (resolved.get$ref() != null) {
            String schemaName = componentName(resolved.get$ref(), "schemas");
            assertTrue(visitedRefs.add(schemaName), () -> "Chuỗi schema $ref bị lặp tại " + schemaName);
            resolved = openApi.getComponents().getSchemas().get(schemaName);
            assertNotNull(resolved, () -> "Không resolve được schema " + schemaName);
            resolvedName = schemaName;
        }
        return resolvedName == null ? "<inline>" : resolvedName;
    }

    private static String componentName(String ref, String componentType) {
        String prefix = "#/components/" + componentType + "/";
        assertTrue(ref.startsWith(prefix), () -> "Không hỗ trợ external/non-component $ref: " + ref);
        return ref.substring(prefix.length());
    }

    protected record RegisteredHandler(
            RequestMappingInfo mappingInfo,
            HandlerMethod handlerMethod) {
    }

    private static Path repositoryRoot() {
        Path candidate = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (candidate != null) {
            if (Files.isRegularFile(candidate.resolve(OPENAPI_DOCUMENT))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new AssertionError("Không tìm thấy repository root chứa " + OPENAPI_DOCUMENT);
    }
}
