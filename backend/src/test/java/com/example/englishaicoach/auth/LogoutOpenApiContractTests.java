package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.englishaicoach.auth.dto.RefreshRequest;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

class LogoutOpenApiContractTests extends OpenApiContractTestSupport {
    @Test void authenticatedLogoutHasNoContentAndDocumentsRetryAndOwnership() throws Exception {
        var api = requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(api, "RefreshTokenRequest", RefreshRequest.class);
        var operation = requireOperation(api, "/auth/logout", PathItem.HttpMethod.POST);
        assertThat(operation.getSecurity()).isNull();
        assertThat(api.getSecurity()).isNotEmpty();
        assertThat(responseStatuses(operation)).containsExactlyInAnyOrder("204", "400", "401");
        assertThat(operation.getResponses().get("204").getContent()).isNull();
        assertThat(operation.getDescription()).contains("Bearer JWT", "already revoked", "AUTH_REFRESH_TOKEN_INVALID",
                "VALIDATION_ERROR", "revoked_at", "last_used_at");
    }
}
