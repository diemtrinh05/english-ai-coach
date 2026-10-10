package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.auth.dto.LoginRequest;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

class LoginOpenApiContractTests extends OpenApiContractTestSupport {
    @Test
    void requestAndPublicResponsesMatchApprovedCredentialsFirstContract() throws Exception {
        var api = requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api, "LoginRequest", LoginRequest.class);
        var schema = requireSchema(api, "LoginRequest");
        assertThat(schema.getRequired()).containsExactlyInAnyOrder("email", "password");
        assertThat(schema.getProperties().get("password").getMinLength()).isEqualTo(1);
        assertThat(schema.getProperties().get("password").getMaxLength()).isEqualTo(100);
        var operation = requireOperation(api, "/auth/login", PathItem.HttpMethod.POST);
        assertThat(operation.getSecurity()).isEmpty();
        assertThat(responseStatuses(operation)).containsExactlyInAnyOrder("200", "401", "423", "429");
        assertThat(operation.getDescription()).contains("Credentials are verified before lock state", "AUTH_INVALID_CREDENTIALS", "AUTH_ACCOUNT_LOCKED");
    }
}
