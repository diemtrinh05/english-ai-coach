package com.example.englishaicoach.core.network;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ApiErrorParser {
    @NonNull public ApiError parse(Response<?> response) {
        return parse(response.code(), response.errorBody());
    }

    @NonNull public ApiError parse(int status, ResponseBody body) {
        String code = null;
        String message = null;
        if (body != null) {
            try {
                JsonElement parsed = JsonParser.parseString(body.string());
                if (parsed.isJsonObject()) {
                    JsonObject object = parsed.getAsJsonObject();
                    code = stringValue(object.get("code"));
                    message = stringValue(object.get("message"));
                }
            } catch (RuntimeException | IOException ignored) {
                // Response lỗi không hợp lệ vẫn giữ nguyên HTTP status để UI xử lý an toàn.
            }
        }
        return new ApiError(status, code, message, kind(status));
    }

    @NonNull public ApiError networkFailure(IOException failure) {
        return new ApiError(0, null, null, ApiError.Kind.OFFLINE);
    }

    private static String stringValue(JsonElement value) {
        return value == null || value.isJsonNull() || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString() ? null : value.getAsString();
    }

    private static ApiError.Kind kind(int status) {
        switch (status) {
            case 400: case 422: return ApiError.Kind.VALIDATION;
            case 401: return ApiError.Kind.UNAUTHORIZED;
            case 403: return ApiError.Kind.FORBIDDEN;
            case 404: return ApiError.Kind.NOT_FOUND;
            case 409: return ApiError.Kind.CONFLICT;
            case 429: return ApiError.Kind.RATE_LIMITED;
            case 500: case 503: return ApiError.Kind.SERVER;
            default: return ApiError.Kind.UNKNOWN;
        }
    }
}
