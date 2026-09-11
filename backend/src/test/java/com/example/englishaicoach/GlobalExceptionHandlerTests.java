package com.example.englishaicoach;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.common.exception.ConcurrentUpdateException;
import com.example.englishaicoach.common.exception.GlobalExceptionHandler;
import com.example.englishaicoach.common.exception.IdempotencyKeyReuseException;

class GlobalExceptionHandlerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsCanonicalValidationError() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.message").value("Yêu cầu không hợp lệ."))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].field").value("name"))
                .andExpect(jsonPath("$.traceId").doesNotExist());
    }

    @Test
    void mapsConcurrentUpdateToConflict() throws Exception {
        mockMvc.perform(get("/test/concurrent-update"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.CONCURRENT_UPDATE))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    void mapsIdempotencyKeyReuseToConflict() throws Exception {
        mockMvc.perform(get("/test/idempotency-key-reuse"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.IDEMPOTENCY_KEY_REUSE));
    }

    @Test
    void mapsExplicitApplicationStatusAndCode() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.NOT_FOUND))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void mapsMalformedJsonToValidationError() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR));
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.INTERNAL_ERROR))
                .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SQL"))));
    }

    @Test
    void mapsMissingRequiredRequestParameterToValidationError() throws Exception {
        mockMvc.perform(get("/test/required-parameter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value("/test/required-parameter"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    void mapsInvalidRequestParameterTypeToValidationError() throws Exception {
        mockMvc.perform(get("/test/numeric-parameter").param("page", "khong-phai-so"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value("/test/numeric-parameter"));
    }

    @Test
    void mapsUnsupportedHttpMethodWithoutFallingBackToInternalError() throws Exception {
        mockMvc.perform(post("/test/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    void mapsUnsupportedMediaTypeWithoutFallingBackToInternalError() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.code").value(ApiErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @RestController
    private static final class TestController {

        @PostMapping(value = "/test/validation", consumes = MediaType.APPLICATION_JSON_VALUE)
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/required-parameter")
        void requiredParameter(@RequestParam String query) {
        }

        @GetMapping("/test/numeric-parameter")
        void numericParameter(@RequestParam Integer page) {
        }

        @GetMapping("/test/concurrent-update")
        void concurrentUpdate() {
            throw new ConcurrentUpdateException();
        }

        @GetMapping("/test/idempotency-key-reuse")
        void idempotencyKeyReuse() {
            throw new IdempotencyKeyReuseException();
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiErrorCodes.NOT_FOUND, "Không tìm thấy.");
        }

        @GetMapping("/test/internal-error")
        void internalError() {
            throw new IllegalStateException("SQL chi tiết nội bộ");
        }
    }

    private record TestRequest(@NotBlank(message = "Tên không được để trống.") String name) {
    }
}
