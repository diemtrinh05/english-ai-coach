package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.AssessmentApi;
import com.example.englishaicoach.data.dto.CurrentUserDto;
import com.example.englishaicoach.domain.model.Assessment;
import com.example.englishaicoach.domain.repository.AssessmentRepository;
import com.google.gson.*;
import java.util.*;
import java.util.function.*;
import retrofit2.*;

public final class RetrofitAssessmentRepository implements AssessmentRepository {
    private final SessionManager sessions;
    private final AssessmentApi api;
    private final ApiErrorParser errors;
    private static final Set<String> LEVELS = Set.of("A1", "A2", "B1", "B2", "C1", "C2");
    public RetrofitAssessmentRepository(SessionManager sessions, AssessmentApi api, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.errors = errors;
    }
    private final class Operation<T> implements Cancelable {
        final String owner;
        final AssessmentRepository.Callback<T> callback;
        Call<?> active;
        boolean canceled;
        Operation(String owner, AssessmentRepository.Callback<T> callback) { this.owner = owner; this.callback = callback; }
        boolean current() { return !canceled && sessions.isCurrentSession(owner); }
        public void cancel() { synchronized (sessions) { canceled = true; if (active != null) active.cancel(); } }
        void request(Supplier<Call<JsonElement>> factory, int status, Consumer<JsonObject> next) {
            synchronized (sessions) {
                if (!current()) return;
                Call<JsonElement> call = factory.get(); active = call;
                call.enqueue(new retrofit2.Callback<JsonElement>() {
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        synchronized (sessions) {
                            try {
                                if (!current() || call.isCanceled()) return;
                                if (response.code() != status) { callback.completed(null, errors.parse(response)); return; }
                                if (response.body() == null || !response.body().isJsonObject()) throw new IllegalArgumentException();
                                next.accept(response.body().getAsJsonObject());
                            } catch (RuntimeException malformed) { if (current()) callback.completed(null, errors.parse(502, null)); }
                            finally { if (response.errorBody() != null) response.errorBody().close(); }
                        }
                    }
                    public void onFailure(Call<JsonElement> call, Throwable failure) {
                        synchronized (sessions) { if (current() && !call.isCanceled()) callback.completed(null, errors.parse(0, null)); }
                    }
                });
            }
        }
        void authenticate(Runnable next) {
            request(api::user, 200, json -> {
                CurrentUserDto user = new Gson().fromJson(json, CurrentUserDto.class);
                if (user == null || !user.valid() || !"USER".equals(user.role)) { callback.completed(null, errors.parse(403, null)); return; }
                next.run();
            });
        }
    }
    public Cancelable start(String owner, Assessment.Start request, AssessmentRepository.Callback<String> callback) {
        Operation<String> op = new Operation<>(owner, callback);
        op.authenticate(() -> op.request(() -> api.start(request), 201, json -> {
            String id = uuid(json, "assessmentId");
            if (!"INITIAL".equals(text(json, "type")) || !"IN_PROGRESS".equals(text(json, "status"))
                    || !LEVELS.contains(text(json, "currentDifficulty"))) throw new IllegalArgumentException();
            callback.completed(id, null);
        })); return op;
    }
    public Cancelable result(String owner, String id, AssessmentRepository.Callback<Assessment.Result> callback) {
        Operation<Assessment.Result> op = new Operation<>(owner, callback);
        op.authenticate(() -> op.request(() -> api.result(id), 200, json -> {
            if (!id.equals(uuid(json, "assessmentId"))) throw new IllegalArgumentException();
            String status = text(json, "status");
            if (!Set.of("IN_PROGRESS", "COMPLETED", "CANCELLED").contains(status)) throw new IllegalArgumentException();
            int answered = integer(json, "totalQuestions");
            int correct = integer(json, "correctAnswers");
            if (answered > 50 || correct > answered || !json.has("score")) throw new IllegalArgumentException();
            if (!json.get("score").isJsonNull()) {
                JsonElement score = json.get("score");
                if (!score.isJsonPrimitive() || !score.getAsJsonPrimitive().isNumber()
                        || score.getAsBigDecimal().signum() < 0 || score.getAsBigDecimal().compareTo(java.math.BigDecimal.valueOf(100)) > 0)
                    throw new IllegalArgumentException();
            }
            callback.completed(new Assessment.Result(id, status, answered), null);
        })); return op;
    }
    public Cancelable question(String owner, String id, AssessmentRepository.Callback<Assessment.Question> callback) {
        Operation<Assessment.Question> op = new Operation<>(owner, callback);
        op.authenticate(() -> op.request(() -> api.question(id), 200, json -> {
            String question = uuid(json, "questionId"), prompt = text(json, "questionText"), level = text(json, "cefrLevel");
            if (!LEVELS.contains(level) || !json.has("options") || !json.get("options").isJsonArray()) throw new IllegalArgumentException();
            List<String> options = new ArrayList<>();
            for (JsonElement value : json.getAsJsonArray("options")) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank()) throw new IllegalArgumentException();
                options.add(value.getAsString());
            }
            if (options.size() != 4 || new HashSet<>(options).size() != 4) throw new IllegalArgumentException();
            callback.completed(new Assessment.Question(question, prompt, options, level), null);
        })); return op;
    }
    public Cancelable answer(String owner, String id, Assessment.Answer request, AssessmentRepository.Callback<Assessment.Ack> callback) {
        Operation<Assessment.Ack> op = new Operation<>(owner, callback);
        // Không GET assessment trước POST retry vì phiên có thể đã đóng nhưng snapshot vẫn replay được.
        op.authenticate(() -> op.request(() -> api.answer(id, request), 200, json -> {
            String level = text(json, "nextDifficulty"); int answered = integer(json, "questionsAnswered");
            if (!LEVELS.contains(level) || answered < 1 || answered > 50) throw new IllegalArgumentException();
            callback.completed(new Assessment.Ack(bool(json, "correct"), bool(json, "isCompleted"), level, answered), null);
        })); return op;
    }
    private static String text(JsonObject json, String key) {
        JsonElement v = json.get(key);
        if (v == null || !v.isJsonPrimitive() || !v.getAsJsonPrimitive().isString() || v.getAsString().isBlank()) throw new IllegalArgumentException();
        return v.getAsString();
    }
    private static String uuid(JsonObject json, String key) { String v = text(json, key); if (!UUID.fromString(v).toString().equals(v)) throw new IllegalArgumentException(); return v; }
    private static int integer(JsonObject json, String key) {
        JsonElement v = json.get(key);
        if (v == null || !v.isJsonPrimitive() || !v.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException();
        int n = v.getAsBigDecimal().intValueExact(); if (n < 0) throw new IllegalArgumentException(); return n;
    }
    private static boolean bool(JsonObject json, String key) {
        JsonElement v = json.get(key);
        if (v == null || !v.isJsonPrimitive() || !v.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException();
        return v.getAsBoolean();
    }
}
