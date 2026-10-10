package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.auth.dto.RefreshRequest;
import com.example.englishaicoach.auth.dto.RefreshResponse;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

class RefreshOpenApiContractTests extends OpenApiContractTestSupport {
    @Test void publicRotationContractRequiresBothTokens() throws Exception {
        var api=requireOpenApi(parseCanonicalOpenApi());assertRecordMatchesSchema(api,"RefreshTokenRequest",RefreshRequest.class);assertRecordMatchesSchema(api,"RefreshResponse",RefreshResponse.class);
        assertThat(requireSchema(api,"RefreshResponse").getRequired()).containsExactlyInAnyOrder("accessToken","expiresIn","tokenType","refreshToken");
        var op=requireOperation(api,"/auth/refresh",PathItem.HttpMethod.POST);assertThat(op.getSecurity()).isEmpty();assertThat(responseStatuses(op)).containsExactlyInAnyOrder("200","401","429");
        assertThat(op.getResponses().get("200").getContent().get("application/json").getSchema().get$ref()).endsWith("/RefreshResponse");
        assertThat(op.getDescription()).contains("fixed expires_at","AUTH_REFRESH_TOKEN_INVALID","AUTH_REFRESH_TOKEN_EXPIRED");
    }
}
