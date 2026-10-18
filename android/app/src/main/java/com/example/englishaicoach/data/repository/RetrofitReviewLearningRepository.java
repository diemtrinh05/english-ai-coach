package com.example.englishaicoach.data.repository;
import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.ReviewLearningApi;
import com.example.englishaicoach.data.dto.CurrentUserDto;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.google.gson.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.*;
import retrofit2.*;
public final class RetrofitReviewLearningRepository implements ReviewLearningRepository {
    private final SessionManager sessions;
    private final ReviewLearningApi api;
    private final ApiErrorParser errors;
    public RetrofitReviewLearningRepository(SessionManager sessions, ReviewLearningApi api, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.errors = errors;
    }
    private final class Operation implements LearningSessionRepository.Cancelable {
        final String owner; final Consumer<ApiError> failure;
        Call<?> active; boolean canceled;
        Operation(String owner, Consumer<ApiError> failure) { this.owner = owner; this.failure = failure; }
        boolean current() { return !canceled && sessions.isCurrentSession(owner); }
        public void cancel() { synchronized (sessions) { canceled = true; if (active != null) active.cancel(); } }
        void request(Supplier<Call<JsonElement>> factory, Consumer<JsonObject> next) {
            synchronized (sessions) {
                if (!current()) return;
                Call<JsonElement> call = factory.get(); active = call;
                call.enqueue(new retrofit2.Callback<JsonElement>() {
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        synchronized (sessions) {
                            try {
                                if (!current() || call.isCanceled()) return;
                                if (response.code() != 200) { failure.accept(errors.parse(response)); return; }
                                if (response.body() == null || !response.body().isJsonObject()) throw new IllegalArgumentException();
                                next.accept(response.body().getAsJsonObject());
                            } catch (RuntimeException malformed) { if (current()) failure.accept(errors.parse(502, null)); }
                            finally { if (response.errorBody() != null) response.errorBody().close(); }
                        }
                    }
                    public void onFailure(Call<JsonElement> call, Throwable failure) {
                        synchronized (sessions) { if (current() && !call.isCanceled()) Operation.this.failure.accept(errors.parse(0, null)); }
                    }
                });
            }
        }
        void authenticate(Runnable next) {
            request(api::user, json -> {
                CurrentUserDto user = new Gson().fromJson(json, CurrentUserDto.class);
                if (user == null || !user.valid() || !"USER".equals(user.role)) { failure.accept(errors.parse(403, null)); return; }
                next.run();
            });
        }
    }
    public LearningSessionRepository.Cancelable reviews(String owner, ReviewLearningRepository.Callback<List<ReviewItem>> callback) {
        Operation op = new Operation(owner, error -> callback.completed(null, error));
        op.authenticate(() -> op.request(api::reviews, json -> {
            JsonArray items = json.getAsJsonArray("items");
            int count = integer(json, "count");
            if (items == null || count != items.size() || count > 50) throw new IllegalArgumentException();
            List<ReviewItem> result = new ArrayList<>();
            for (JsonElement row : items) {
                JsonObject item = row.getAsJsonObject(); String id = uuid(item, "vocabularyId");
                result.add(new ReviewItem(id, text(item, "word"), status(item), instant(item, "nextReviewAt")));
            }
            callback.completed(List.copyOf(result), null);
        })); return op;
    }
    public LearningSessionRepository.Cancelable progress(String owner, String vocabulary, ReviewLearningRepository.Callback<VocabularyProgress> callback) {
        Operation op = new Operation(owner, error -> callback.completed(null, error));
        op.authenticate(() -> op.request(() -> api.progress(vocabulary), json -> {
            if (!vocabulary.equals(uuid(json, "vocabularyId"))) throw new IllegalArgumentException();
            if (!json.has("nextReviewAt")) throw new IllegalArgumentException();
            String next = json.get("nextReviewAt").isJsonNull() ? null : instant(json, "nextReviewAt");
            callback.completed(new VocabularyProgress(vocabulary, status(json), next, number(json, "easeFactor"),
                    integer(json, "intervalDays"), integer(json, "repetitions"), integer(json, "correctCount"), integer(json, "incorrectCount")), null);
        })); return op;
    }
    public LearningSessionRepository.Cancelable complete(String owner, String session, String event, ReviewLearningRepository.Callback<LearningSummary> callback) {
        Operation op = new Operation(owner, error -> callback.completed(null, error));
        // Không đọc trạng thái open trước POST: session đã đóng vẫn có thể replay snapshot.
        op.authenticate(() -> op.request(() -> api.complete(session, Map.of("eventId", event)), json -> callback.completed(map(json, session, true), null)));
        return op;
    }
    public LearningSessionRepository.Cancelable summary(String owner, String session, ReviewLearningRepository.Callback<LearningSummary> callback) {
        Operation op = new Operation(owner, error -> callback.completed(null, error));
        op.authenticate(() -> op.request(() -> api.session(session), json -> callback.completed(map(json, session, true), null)));
        return op;
    }
    private static LearningSummary map(JsonObject json, String session, boolean closed) {
        if (!session.equals(uuid(json, "sessionId"))) throw new IllegalArgumentException();
        String type = text(json, "sessionType");
        if (!Set.of("MIXED", "REVIEW", "NEW_WORDS", "QUIZ").contains(type)) throw new IllegalArgumentException();
        String end = instant(json, "endedAt");
        int words = integer(json, "wordsStudiedCount"); BigDecimal accuracy = number(json, "accuracyPercent");
        if (closed && words < 1 || accuracy.compareTo(BigDecimal.valueOf(100)) > 0) throw new IllegalArgumentException();
        return new LearningSummary(session, type, instant(json, "startedAt"), end, words, accuracy);
    }
    private static String status(JsonObject json) {
        String value = text(json, "status");
        if (!Set.of("NEW", "LEARNING", "REVIEWING", "MASTERED").contains(value)) throw new IllegalArgumentException(); return value;
    }
    private static String uuid(JsonObject json, String key) { String value = text(json, key); UUID.fromString(value); return value; }
    private static String instant(JsonObject json, String key) { String value = text(json, key); Instant.parse(value); return value; }
    private static String text(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank()) throw new IllegalArgumentException(); return value.getAsString();
    }
    private static BigDecimal number(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException();
        BigDecimal result = value.getAsBigDecimal(); if (result.signum() < 0) throw new IllegalArgumentException(); return result;
    }
    private static int integer(JsonObject json, String key) { return number(json, key).intValueExact(); }
}
