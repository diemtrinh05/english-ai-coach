package com.example.englishaicoach.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.onboarding.dto.*;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

class GoalOpenApiContractTests extends OpenApiContractTestSupport {
    @Test void goalDtosStatusesAuthenticationAndOptionalPrimaryMatchApprovedContract() throws Exception {
        var api=requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api,"GoalResponse",GoalResponse.class);
        assertRecordMatchesSchema(api,"UserGoalResponse",UserGoalResponse.class);
        assertRecordMatchesSchema(api,"UserGoalsResponse",UserGoalsResponse.class);
        assertRecordMatchesSchema(api,"SetUserGoalsRequest",SetUserGoalsRequest.class);
        var catalog=requireOperation(api,"/goals",PathItem.HttpMethod.GET);
        var get=requireOperation(api,"/users/me/goals",PathItem.HttpMethod.GET);
        var put=requireOperation(api,"/users/me/goals",PathItem.HttpMethod.PUT);
        assertThat(catalog.getSecurity()).isEmpty();assertThat(get.getSecurity()).isNull();assertThat(put.getSecurity()).isNull();assertThat(api.getSecurity()).isNotEmpty();
        assertThat(responseStatuses(get)).containsExactlyInAnyOrder("200","401");
        assertThat(responseStatuses(put)).containsExactlyInAnyOrder("200","400","401","404");
        var schema=api.getComponents().getSchemas().get("SetUserGoalsRequest");assertThat(schema.getRequired()).containsExactly("goalIds");
        var ids=(Schema<?>)schema.getProperties().get("goalIds");assertThat(ids.getMinItems()).isEqualTo(1);assertThat(ids.getUniqueItems()).isTrue();
        var primary=(Schema<?>)schema.getProperties().get("primaryGoalId");assertThat(primary.getTypes()).containsExactlyInAnyOrder("string","null");
        assertThat(get.getDescription()).contains("inactive","goals.id","name,id");assertThat(put.getDescription()).contains("Atomic","no primary","serialize");
    }
}
