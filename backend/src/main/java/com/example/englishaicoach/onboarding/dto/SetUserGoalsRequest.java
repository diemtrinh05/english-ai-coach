package com.example.englishaicoach.onboarding.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public record SetUserGoalsRequest(
        @NotNull(message = "Danh sách mục tiêu không được để trống.")
        @Size(min = 1, message = "Cần chọn ít nhất một mục tiêu.") @JsonDeserialize(contentUsing = StrictUuid.class) List<@NotNull UUID> goalIds,
        @JsonDeserialize(using = StrictUuid.class) UUID primaryGoalId) {
    // Chỉ nhận UUID string chuẩn OpenAPI, không nhận dạng rút gọn/Base64 hoặc kiểu JSON khác.
    public static class StrictUuid extends ValueDeserializer<UUID> {
        @Override public UUID deserialize(JsonParser parser, DeserializationContext context) {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) return context.reportInputMismatch(UUID.class,"Mục tiêu phải là UUID string.");
            String value=parser.getString();
            if (!value.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                return context.reportInputMismatch(UUID.class,"UUID mục tiêu không hợp lệ.");
            }
            return UUID.fromString(value);
        }
    }
}
