package com.example.englishaicoach.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.englishaicoach.support.OpenApiContractTestSupport;
import com.example.englishaicoach.vocabulary.dto.TopicResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TopicOpenApiContractTests extends OpenApiContractTestSupport {

    @Test
    void topicOperationsMatchApprovedPublicReadContract() throws IOException {
        var parsed = parseCanonicalOpenApi();
        assertEquals(List.of(), parsed.getMessages());
        OpenAPI openApi = requireOpenApi(parsed);
        Operation list = requireOperation(openApi, "/topics", PathItem.HttpMethod.GET);
        Operation detail = requireOperation(openApi, "/topics/{topicId}", PathItem.HttpMethod.GET);

        assertEquals(List.of(), list.getSecurity());
        assertEquals(List.of(), detail.getSecurity());
        assertEquals("listTopics", list.getOperationId());
        assertEquals("getTopic", detail.getOperationId());
        assertEquals(Set.of("page", "size", "search", "parentTopicId"),
                list.getParameters().stream().map(parameter -> parameter.getName() == null
                        ? openApi.getComponents().getParameters().get(
                                parameter.get$ref().substring(parameter.get$ref().lastIndexOf('/') + 1)).getName()
                        : parameter.getName()).collect(java.util.stream.Collectors.toSet()));
        assertEquals(Set.of("200"), responseStatuses(list));
        assertEquals(Set.of("200", "404"), responseStatuses(detail));
        assertEquals("#/components/schemas/PageTopicResponse",
                list.getResponses().get("200").getContent().get("application/json").getSchema().get$ref());
        assertEquals("#/components/schemas/TopicResponse",
                detail.getResponses().get("200").getContent().get("application/json").getSchema().get$ref());
        assertNotNull(detail.getResponses().get("404"));
        assertRecordMatchesSchema(openApi, "TopicResponse", TopicResponse.class);
    }
}
