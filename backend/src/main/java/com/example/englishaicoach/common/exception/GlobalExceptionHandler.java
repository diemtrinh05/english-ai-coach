package com.example.englishaicoach.common.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {
        return response(
                exception.getStatus(),
                exception.getCode(),
                exception.getMessage(),
                request,
                exception.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<Map<String, Object>> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::validationDetail)
                .toList();
        return validationResponse(request, details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {
        return validationResponse(request, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return validationResponse(request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        return validationResponse(request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return validationResponse(request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.METHOD_NOT_ALLOWED,
                ApiErrorCodes.VALIDATION_ERROR,
                "Phương thức HTTP không được hỗ trợ cho tài nguyên này.",
                request,
                List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ApiErrorCodes.VALIDATION_ERROR,
                "Định dạng dữ liệu yêu cầu không được hỗ trợ.",
                request,
                List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<Map<String, Object>> details = exception.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("field", violation.getPropertyPath().toString());
                    detail.put("message", violation.getMessage());
                    return detail;
                })
                .toList();
        return validationResponse(request, details);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.NOT_FOUND,
                ApiErrorCodes.NOT_FOUND,
                "Không tìm thấy tài nguyên.",
                request,
                List.of());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringRequestException(
            ErrorResponseException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return response(
                status,
                codeFor(status),
                messageFor(status),
                request,
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCodes.INTERNAL_ERROR,
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                request,
                List.of());
    }

    private ResponseEntity<ApiErrorResponse> validationResponse(
            HttpServletRequest request,
            List<Map<String, Object>> details) {
        return response(
                HttpStatus.BAD_REQUEST,
                ApiErrorCodes.VALIDATION_ERROR,
                "Yêu cầu không hợp lệ.",
                request,
                details);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<Map<String, Object>> details) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                details);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> validationDetail(FieldError error) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("field", error.getField());
        detail.put("message", error.getDefaultMessage() == null
                ? "Giá trị không hợp lệ."
                : error.getDefaultMessage());
        return detail;
    }

    private String codeFor(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> ApiErrorCodes.UNAUTHORIZED;
            case FORBIDDEN -> ApiErrorCodes.FORBIDDEN;
            case NOT_FOUND -> ApiErrorCodes.NOT_FOUND;
            case CONFLICT -> ApiErrorCodes.CONFLICT;
            case TOO_MANY_REQUESTS -> ApiErrorCodes.RATE_LIMITED;
            default -> status.is4xxClientError()
                    ? ApiErrorCodes.VALIDATION_ERROR
                    : ApiErrorCodes.INTERNAL_ERROR;
        };
    }

    private String messageFor(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "Yêu cầu xác thực hợp lệ.";
            case FORBIDDEN -> "Bạn không có quyền thực hiện thao tác này.";
            case NOT_FOUND -> "Không tìm thấy tài nguyên.";
            case CONFLICT -> "Yêu cầu xung đột với trạng thái hiện tại.";
            case TOO_MANY_REQUESTS -> "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau.";
            default -> status.is4xxClientError()
                    ? "Yêu cầu không hợp lệ."
                    : "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.";
        };
    }
}
