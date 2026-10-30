package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.domain.model.Assessment;
import com.google.gson.*;
import java.util.Set;
import java.util.UUID;

final class AssessmentResponseParser {
    private AssessmentResponseParser() { }
    static Assessment.Level level(JsonElement element) {
        JsonObject data = element.getAsJsonObject();
        String id = string(data, "id"), code = string(data, "code"), name = string(data, "name");
        if (!UUID.fromString(id).toString().equals(id) || !Set.of("A1", "A2", "B1", "B2", "C1", "C2").contains(code))
            throw new JsonParseException("Invalid level");
        if (!data.has("sortOrder") || !data.get("sortOrder").isJsonPrimitive()
                || !data.getAsJsonPrimitive("sortOrder").isNumber()) throw new JsonParseException("Invalid level order");
        data.get("sortOrder").getAsBigDecimal().intValueExact();
        String description = null;
        if (data.has("description") && !data.get("description").isJsonNull()) {
            JsonElement value = data.get("description");
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new JsonParseException("Invalid description");
            description = value.getAsString();
        }
        return new Assessment.Level(code, name, description);
    }
    private static String string(JsonObject data, String key) {
        JsonElement value = data.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank())
            throw new JsonParseException("Invalid level field");
        return value.getAsString();
    }
}
