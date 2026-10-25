package com.example.englishaicoach.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.assessment.dto.AssessmentQuestionResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResponse;
import com.example.englishaicoach.assessment.dto.AssessmentResultResponse;
import com.example.englishaicoach.assessment.dto.StartAssessmentRequest;
import com.example.englishaicoach.assessment.dto.SubmitAssessmentAnswerRequest;
import com.example.englishaicoach.assessment.dto.AssessmentAnswerResponse;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

class AssessmentOpenApiContractTests extends OpenApiContractTestSupport {
    @Test void answerContractRequiresAllFourFieldsAndDeclaresApprovedErrors() throws Exception {
        var api = requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api, "SubmitAssessmentAnswerRequest", SubmitAssessmentAnswerRequest.class);
        assertRecordMatchesSchema(api, "AssessmentAnswerResponse", AssessmentAnswerResponse.class);
        assertThat(requireSchema(api, "SubmitAssessmentAnswerRequest").getRequired())
                .containsExactlyInAnyOrder("questionId", "answer", "responseTimeMs", "eventId");
        assertThat(requireSchema(api, "SubmitAssessmentAnswerRequest").getAdditionalProperties()).isEqualTo(false);
        var timing = requireSchema(api, "SubmitAssessmentAnswerRequest").getProperties().get("responseTimeMs");
        assertThat(timing.getFormat()).isEqualTo("int32");
        assertThat(timing.getMinimum()).isEqualByComparingTo("0");
        assertThat(timing.getMaximum()).isEqualByComparingTo("2147483647");
        assertThat(requireSchema(api, "AssessmentAnswerResponse").getRequired())
                .containsExactlyInAnyOrder("correct", "nextDifficulty", "questionsAnswered", "isCompleted");
        var operation = requireOperation(api, "/assessments/{assessmentId}/answers", PathItem.HttpMethod.POST);
        assertThat(responseStatuses(operation)).containsExactlyInAnyOrder("200", "400", "401", "403", "404", "409", "503");
        assertThat(operation.getDescription()).contains("USER", "persisted", "Replay", "atomically", "nextDifficulty");
        assertThat(operation.getSecurity()).isNull();
    }

    @Test void assessmentDtosMatchCanonicalFieldSetsAndNullableScore() throws Exception {
        var api = requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api, "StartAssessmentRequest", StartAssessmentRequest.class);
        assertRecordMatchesSchema(api, "AssessmentResponse", AssessmentResponse.class);
        assertRecordMatchesSchema(api, "AssessmentQuestionResponse", AssessmentQuestionResponse.class);
        assertRecordMatchesSchema(api, "AssessmentResultResponse", AssessmentResultResponse.class);
        var result = requireSchema(api, "AssessmentResultResponse");
        assertThat(result.getRequired()).contains("score");
        assertThat(result.getProperties().get("score").getTypes()).containsExactlyInAnyOrder("number", "null");
        var options = requireSchema(api, "AssessmentQuestionResponse").getProperties().get("options");
        assertThat(options.getMinItems()).isEqualTo(4);
        assertThat(options.getMaxItems()).isEqualTo(4);
        assertThat(options.getUniqueItems()).isTrue();
        assertThat(requireSchema(api, "AssessmentQuestionResponse").getProperties()).doesNotContainKey("correctAnswer");
        assertThat(requireSchema(api, "StartAssessmentRequest").getAdditionalProperties()).isEqualTo(false);
    }

    @Test void startAndOwnedReadOperationsDeclareCanonicalStatusesAndGuards() throws Exception {
        var api = requireOpenApi(parseCanonicalOpenApi());
        var start = requireOperation(api, "/assessments", PathItem.HttpMethod.POST);
        assertThat(responseStatuses(start)).containsExactlyInAnyOrder("201", "400", "401", "403", "409", "503");
        assertThat(start.getDescription()).contains("USER", "INITIAL", "PERIODIC", "MANUAL", "replay", "transaction");
        var get = requireOperation(api, "/assessments/{assessmentId}", PathItem.HttpMethod.GET);
        assertThat(responseStatuses(get)).containsExactlyInAnyOrder("200", "400", "401", "403", "404");
        var next = requireOperation(api, "/assessments/{assessmentId}/next-question", PathItem.HttpMethod.GET);
        assertThat(responseStatuses(next)).containsExactlyInAnyOrder("200", "400", "401", "403", "404", "503");
        assertThat(next.getDescription()).contains("persisted", "COMPLETED", "CANCELLED", "ASSESSMENT_CONTENT_UNAVAILABLE", "never generates");
        assertThat(start.getSecurity()).isNull();
        assertThat(get.getSecurity()).isNull();
        assertThat(next.getSecurity()).isNull();
        assertThat(api.getSecurity()).isNotEmpty();
    }
}
