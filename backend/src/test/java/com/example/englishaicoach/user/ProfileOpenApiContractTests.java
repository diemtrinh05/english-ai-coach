package com.example.englishaicoach.user;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.user.dto.UpdateProfileRequest;
import com.example.englishaicoach.user.dto.UserProfileResponse;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

class ProfileOpenApiContractTests extends OpenApiContractTestSupport {
    @Test void userAndProfileContractsHaveAuthenticatedOwnershipAndApprovedStatuses() throws Exception {
        var api=requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api,"AuthUserSummary",AuthUserSummary.class);
        assertRecordMatchesSchema(api,"UpdateProfileRequest",UpdateProfileRequest.class);
        assertRecordMatchesSchema(api,"UserProfileResponse",UserProfileResponse.class);
        var user=requireOperation(api,"/users/me",PathItem.HttpMethod.GET);
        var get=requireOperation(api,"/users/me/profile",PathItem.HttpMethod.GET);
        var put=requireOperation(api,"/users/me/profile",PathItem.HttpMethod.PUT);
        assertThat(api.getSecurity()).isNotEmpty();assertThat(user.getSecurity()).isNull();assertThat(get.getSecurity()).isNull();assertThat(put.getSecurity()).isNull();
        assertThat(responseStatuses(get)).containsExactlyInAnyOrder("200","401","404");
        assertThat(responseStatuses(put)).containsExactlyInAnyOrder("200","400","401");
        var schema=api.getComponents().getSchemas().get("UpdateProfileRequest");
        assertThat(schema.getRequired()).containsExactlyInAnyOrder("fullName","dailyLearningMinutes","timezone");
        assertThat(((io.swagger.v3.oas.models.media.Schema<?>)schema.getProperties().get("fullName")).getMinLength()).isEqualTo(1);
        assertThat(((io.swagger.v3.oas.models.media.Schema<?>)schema.getProperties().get("timezone")).getDescription()).contains("ZoneId.getAvailableZoneIds()","UTC");
        assertThat(get.getDescription()).contains("404 NOT_FOUND","without creating defaults");
        assertThat(put.getDescription()).contains("create-or-update","CEFR","avatarUrl");
    }
}
