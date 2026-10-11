package com.example.englishaicoach.data.dto;

import com.google.gson.*;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import java.util.*;

public final class OnboardingDtos {
    private OnboardingDtos() { }
    public static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            throw new JsonParseException("Invalid response field");
        return value.getAsString();
    }
    public static Profile profile(JsonElement json) {
        JsonObject o = json.getAsJsonObject();
        String name = string(o, "fullName"), zone = string(o, "timezone");
        JsonElement value = o.get("dailyLearningMinutes");
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
                || !value.getAsString().matches("[0-9]+")) throw new JsonParseException("Invalid minutes");
        int minutes = value.getAsInt();
        if (name.trim().isEmpty() || name.length() > 100 || zone.isEmpty() || zone.length() > 50 || minutes < 5 || minutes > 180)
            throw new JsonParseException("Invalid profile");
        String avatar = o.has("avatarUrl") && !o.get("avatarUrl").isJsonNull() ? string(o, "avatarUrl") : null;
        return new Profile(name, minutes, zone, avatar);
    }
    public static List<Goal> catalog(JsonElement json) {
        List<Goal> items = new ArrayList<>(); Set<String> seen = new HashSet<>();
        for (JsonElement element : json.getAsJsonArray()) {
            JsonObject o = element.getAsJsonObject(); String id = uuid(o);
            if (!seen.add(id)) throw new JsonParseException("Duplicate goal");
            items.add(new Goal(id, string(o, "name"), true));
        }
        return items;
    }
    public static Selection selection(JsonElement json, List<Goal> catalog) {
        List<String> ids = new ArrayList<>(); String primary = null;
        for (JsonElement element : json.getAsJsonObject().getAsJsonArray("goals")) {
            JsonObject o = element.getAsJsonObject(); String id = uuid(o);
            if (ids.contains(id)) throw new JsonParseException("Duplicate selection");
            JsonElement flag = o.get("isPrimary");
            if (flag == null || !flag.isJsonPrimitive() || !flag.getAsJsonPrimitive().isBoolean())
                throw new JsonParseException("Invalid primary");
            if (flag.getAsBoolean()) {
                if (primary != null) throw new JsonParseException("Multiple primary"); primary = id;
            }
            ids.add(id);
            if (catalog.stream().noneMatch(goal -> goal.id.equals(id))) catalog.add(new Goal(id, string(o, "name"), false));
        }
        return new Selection(ids, primary);
    }
    private static String uuid(JsonObject object) {
        String id = string(object, "id");
        if (!id.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))
            throw new JsonParseException("Invalid goal ID");
        return id;
    }
}
