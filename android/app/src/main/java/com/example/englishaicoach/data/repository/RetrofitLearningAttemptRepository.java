package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.LearningAttemptApi;
import com.example.englishaicoach.data.dto.CurrentUserDto;
import com.example.englishaicoach.domain.model.LearningAttempt;
import com.example.englishaicoach.domain.repository.*;
import com.google.gson.*;
import java.time.Instant;
import java.util.*;
import java.util.function.*;
import retrofit2.*;

public final class RetrofitLearningAttemptRepository implements LearningAttemptRepository {
    private final SessionManager sessions;
    private final LearningAttemptApi api;
    private final ApiErrorParser errors;
    public RetrofitLearningAttemptRepository(SessionManager sessions, LearningAttemptApi api, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.errors = errors;
    }
    private final class Operation implements LearningSessionRepository.Cancelable {
        final String owner;
        final Consumer<ApiError> failure;
        Call<?> active;
        boolean canceled;
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
    public LearningSessionRepository.Cancelable submit(String owner, LearningAttempt.Request request, LearningAttemptRepository.Callback callback) {
        Operation op = new Operation(owner, error -> callback.completed(null, error));
        // Replay phải tới POST sau auth, không bị GET session/progress hiện tại chặn.
        op.authenticate(() -> op.request(() -> api.submit(request), json -> callback.completed(map(json, request), null)));
        return op;
    }
    public LearningSessionRepository.Cancelable refresh(String owner, String session, String vocabulary,
            RefreshCallback callback) {
        Operation op = new Operation(owner, error -> callback.completed(false, error));
        op.authenticate(() -> op.request(() -> api.progress(vocabulary), json -> {
            progress(json, vocabulary);
            op.request(() -> api.session(session), value -> {
                if (!session.equals(text(value, "sessionId"))) throw new IllegalArgumentException();
                Instant.parse(text(value, "startedAt"));
                if (!Set.of("MIXED", "NEW_WORDS", "REVIEW", "QUIZ").contains(text(value, "sessionType"))) throw new IllegalArgumentException();
                boolean open = !value.has("endedAt") || value.get("endedAt").isJsonNull();
                if (!open) Instant.parse(text(value, "endedAt"));
                callback.completed(open, null);
            });
        }));
        return op;
    }
    private static LearningAttempt map(JsonObject json, LearningAttempt.Request request) {
        String id = text(json, "attemptId"); UUID.fromString(id);
        int quality = integer(json, "answerQuality");
        if (quality != request.answerQuality) throw new IllegalArgumentException();
        JsonElement correct = json.get("isCorrect");
        if (correct == null || !correct.isJsonPrimitive() || !correct.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException();
        JsonObject srs = json.getAsJsonObject("srs");
        number(srs, "oldEaseFactor"); number(srs, "newEaseFactor");
        integer(srs, "oldIntervalDays"); int interval = integer(srs, "newIntervalDays");
        integer(srs, "oldRepetitions"); integer(srs, "newRepetitions");
        String next = text(srs, "nextReviewAt"); Instant.parse(next); text(srs, "algorithmVersion");
        progress(json.getAsJsonObject("progress"), request.vocabularyId);
        return new LearningAttempt(id, correct.getAsBoolean(), quality, next, interval);
    }
    private static void progress(JsonObject json, String vocabulary) {
        if (!vocabulary.equals(text(json, "vocabularyId"))) throw new IllegalArgumentException();
        if (!Set.of("NEW", "LEARNING", "REVIEWING", "MASTERED").contains(text(json, "status"))) throw new IllegalArgumentException();
        number(json, "easeFactor"); integer(json, "intervalDays"); integer(json, "repetitions");
        integer(json, "correctCount"); integer(json, "incorrectCount");
        if (!json.has("nextReviewAt")) throw new IllegalArgumentException();
        if (!json.get("nextReviewAt").isJsonNull()) Instant.parse(text(json, "nextReviewAt"));
    }
    private static String text(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank()) throw new IllegalArgumentException();
        return value.getAsString();
    }
    private static java.math.BigDecimal number(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException();
        java.math.BigDecimal number = value.getAsBigDecimal();
        if (number.signum() < 0) throw new IllegalArgumentException();
        return number;
    }
    private static int integer(JsonObject json, String key) { return number(json, key).intValueExact(); }
}
