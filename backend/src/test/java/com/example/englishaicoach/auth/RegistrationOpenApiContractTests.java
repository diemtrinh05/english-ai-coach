package com.example.englishaicoach.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.englishaicoach.auth.dto.AuthResponse;
import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.auth.dto.RegisterRequest;
import com.example.englishaicoach.support.OpenApiContractTestSupport;
import io.swagger.v3.oas.models.PathItem;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RegistrationOpenApiContractTests extends OpenApiContractTestSupport {
    @Test
    void dtoPropertiesRequiredFieldsAndResponseStatusesMatchCanonicalOpenApi() throws Exception {
        var openApi = requireOpenApi(parseCanonicalOpenApi());
        assertRecordMatchesSchema(openApi, "RegisterRequest", RegisterRequest.class);
        assertRecordMatchesSchema(openApi, "AuthUserSummary", AuthUserSummary.class);
        var authSchema = requireSchema(openApi, "AuthResponse");
        var tokenSchema = requireSchema(openApi, "TokenResponse");
        var fields = new HashSet<>(tokenSchema.getProperties().keySet());
        fields.addAll(authSchema.getAllOf().get(1).getProperties().keySet());
        assertThat(Arrays.stream(AuthResponse.class.getRecordComponents()).map(component -> component.getName())
                .collect(Collectors.toSet())).isEqualTo(fields);
        var request = requireSchema(openApi, "RegisterRequest");
        assertThat(request.getRequired()).containsExactlyInAnyOrder("email", "password", "fullName");
        assertThat(request.getProperties().get("password").getMinLength()).isEqualTo(8);
        assertThat(request.getProperties().get("password").getMaxLength()).isEqualTo(100);
        var operation = requireOperation(openApi, "/auth/register", PathItem.HttpMethod.POST);
        assertThat(operation.getSecurity()).isEmpty();
        assertThat(responseStatuses(operation)).containsExactlyInAnyOrder("201", "400", "409");
        assertThat(RegistrationController.class.getMethod("register", RegisterRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value().value())
                .isEqualTo(201);
    }
}
