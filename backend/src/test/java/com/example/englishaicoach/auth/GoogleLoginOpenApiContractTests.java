package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.auth.dto.GoogleLoginRequest;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

class GoogleLoginOpenApiContractTests extends OpenApiContractTestSupport {
    @Test void googleRequestAndPublicResponsesMatchOwnerApprovedContract() throws Exception {
        var api=requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api,"GoogleLoginRequest",GoogleLoginRequest.class);
        assertThat(requireSchema(api,"GoogleLoginRequest").getRequired()).containsExactly("idToken");
        var operation=requireOperation(api,"/auth/google",PathItem.HttpMethod.POST);
        assertThat(operation.getSecurity()).isEmpty();
        assertThat(responseStatuses(operation)).containsExactlyInAnyOrder("200","400","401","409","423","429","503");
        assertThat(operation.getDescription()).contains("GOOGLE_CLIENT_ID","UNAUTHORIZED","CONFLICT","AUTH_ACCOUNT_LOCKED","INTERNAL_ERROR");
        assertThat(new GoogleLoginRequest("private-token").toString()).doesNotContain("private-token");
    }
}
