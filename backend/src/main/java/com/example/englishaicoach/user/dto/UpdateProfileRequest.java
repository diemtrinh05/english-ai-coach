package com.example.englishaicoach.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ tên không được để trống.")
        @Size(min = 1, max = 100, message = "Họ tên phải có 1 đến 100 ký tự.") @JsonDeserialize(using = StrictString.class) String fullName,
        @JsonDeserialize(using = StrictString.class) String avatarUrl,
        @NotNull(message = "Thời gian học không được để trống.")
        @Min(value = 5, message = "Thời gian học tối thiểu là 5 phút.")
        @Max(value = 180, message = "Thời gian học tối đa là 180 phút.") @JsonDeserialize(using = StrictInteger.class) Integer dailyLearningMinutes,
        @NotBlank(message = "Múi giờ không được để trống.")
        @Size(max = 50, message = "Múi giờ không được vượt quá 50 ký tự.") @JsonDeserialize(using = StrictString.class) String timezone) {
    // Kiểm tra đúng kiểu JSON theo OpenAPI, không ép số thập phân hoặc chuỗi thành integer.
    public static class StrictInteger extends ValueDeserializer<Integer> {
        @Override public Integer deserialize(JsonParser parser, DeserializationContext context) {
            if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                return context.reportInputMismatch(Integer.class, "Thời gian học phải là JSON integer.");
            }
            return parser.getIntValue();
        }
    }

    public static class StrictString extends ValueDeserializer<String> {
        @Override public String deserialize(JsonParser parser, DeserializationContext context) {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return context.reportInputMismatch(String.class, "Trường phải là JSON string.");
            }
            return parser.getString();
        }
    }

    @Override public String toString() { return "UpdateProfileRequest[profile=REDACTED]"; }
}
