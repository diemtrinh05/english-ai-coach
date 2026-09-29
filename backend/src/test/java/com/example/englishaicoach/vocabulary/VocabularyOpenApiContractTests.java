package com.example.englishaicoach.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.englishaicoach.support.OpenApiContractTestSupport;
import com.example.englishaicoach.vocabulary.dto.VocabularyExampleResponse;
import com.example.englishaicoach.vocabulary.dto.VocabularyResponse;
import io.swagger.v3.oas.models.PathItem;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class VocabularyOpenApiContractTests extends OpenApiContractTestSupport {

    @Test
    void publicVocabularyOperationsAndDtosMatchApprovedContract() throws IOException {
        var parsed = parseCanonicalOpenApi();
        assertEquals(List.of(), parsed.getMessages());
        var openApi = requireOpenApi(parsed);
        var list = requireOperation(openApi, "/vocabulary", PathItem.HttpMethod.GET);
        var detail = requireOperation(openApi, "/vocabulary/{vocabularyId}", PathItem.HttpMethod.GET);
        var examples = requireOperation(openApi, "/vocabulary/{vocabularyId}/examples", PathItem.HttpMethod.GET);

        assertEquals(List.of(), list.getSecurity());
        assertEquals(List.of(), detail.getSecurity());
        assertEquals(List.of(), examples.getSecurity());
        assertEquals("listVocabulary", list.getOperationId());
        assertEquals("getVocabulary", detail.getOperationId());
        assertEquals("getVocabularyExamples", examples.getOperationId());
        assertEquals(Set.of("page", "size", "search", "cefr", "topicId", "partOfSpeech", "sort"),
                list.getParameters().stream().map(parameter -> parameter.getName() == null
                        ? openApi.getComponents().getParameters().get(
                                parameter.get$ref().substring(parameter.get$ref().lastIndexOf('/') + 1)).getName()
                        : parameter.getName()).collect(Collectors.toSet()));
        assertEquals(Set.of("200"), responseStatuses(list));
        assertEquals(Set.of("200", "404"), responseStatuses(detail));
        assertEquals(Set.of("200", "404"), responseStatuses(examples));
        assertRecordMatchesSchema(openApi, "VocabularyResponse", VocabularyResponse.class);
        assertRecordMatchesSchema(openApi, "VocabularyExampleResponse", VocabularyExampleResponse.class);
    }
}
