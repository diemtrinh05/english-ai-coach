package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.data.api.LearningSessionApi;
import com.example.englishaicoach.data.api.VocabularyApi;
import com.example.englishaicoach.data.dto.CurrentUserDto;
import com.example.englishaicoach.data.dto.VocabularyDto;
import com.example.englishaicoach.data.mapper.VocabularyMapper;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.LearningSessionRepository;
import com.google.gson.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import retrofit2.*;

public final class RetrofitLearningSessionRepository implements LearningSessionRepository {
    private final SessionManager sessions;
    private final LearningSessionApi api;
    private final VocabularyApi vocabulary;
    private final ApiErrorParser errors;
    public RetrofitLearningSessionRepository(SessionManager sessions, LearningSessionApi api,
            VocabularyApi vocabulary, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.vocabulary = vocabulary; this.errors = errors;
    }
    private final class Operation implements Cancelable {
        final String owner;
        final LearningSessionRepository.Callback callback;
        Call<?> active;
        LearningSession session;
        boolean canceled;
        Operation(String owner, LearningSessionRepository.Callback callback) { this.owner = owner; this.callback = callback; }
        boolean current() { return !canceled && sessions.isCurrentSession(owner); }
        @Override public void cancel() { synchronized (sessions) { canceled = true; if (active != null) active.cancel(); } }
        void fail(int status) { if (current()) callback.completed(new Result(session, null, errors.parse(status, null))); }
        <T> void request(Supplier<Call<T>> factory, int expected, Consumer<T> next) {
            synchronized (sessions) {
                if (!current()) return;
                Call<T> call = factory.get(); active = call;
                call.enqueue(new retrofit2.Callback<T>() {
                    @Override public void onResponse(Call<T> call, Response<T> response) {
                        synchronized (sessions) {
                            try {
                                if (!current() || call.isCanceled()) return;
                                if (response.code() != expected) {
                                    callback.completed(new Result(session, null, errors.parse(response))); return;
                                }
                                if (response.body() == null) { fail(502); return; }
                                next.accept(response.body());
                            } catch (RuntimeException invalid) { fail(502); }
                            finally { if (response.errorBody() != null) response.errorBody().close(); }
                        }
                    }
                    @Override public void onFailure(Call<T> call, Throwable failure) {
                        synchronized (sessions) { if (current() && !call.isCanceled()) fail(0); }
                    }
                });
            }
        }
    }
    @Override public Cancelable load(String owner, String event, String vocabularyId,
            String sessionId, boolean start, LearningSessionRepository.Callback callback) {
        return load(owner, event, vocabularyId, sessionId, start, "MIXED", callback);
    }
    @Override public Cancelable load(String owner, String event, String vocabularyId,
            String sessionId, boolean start, String sessionType, LearningSessionRepository.Callback callback) {
        if (!Set.of("MIXED", "REVIEW").contains(sessionType)) throw new IllegalArgumentException();
        synchronized (sessions) {
            Operation op = new Operation(owner, callback);
            op.request(api::user, 200, user -> {
                CurrentUserDto dto = new Gson().fromJson(user, CurrentUserDto.class);
                if (dto == null || !dto.valid() || !"USER".equals(dto.role)) { op.fail(403); return; }
                Supplier<Call<JsonElement>> action = start
                        ? () -> api.start(Map.of("sessionType", sessionType, "eventId", event))
                        : () -> api.get(sessionId);
                op.request(action, start ? 201 : 200, body -> {
                    JsonObject json = body.getAsJsonObject();
                    String id = required(json, "sessionId"), type = required(json, "sessionType");
                    UUID.fromString(id);
                    String began = required(json, "startedAt"); java.time.Instant.parse(began);
                    String ended = json.has("endedAt") && !json.get("endedAt").isJsonNull()
                            ? required(json, "endedAt") : null;
                    if (ended != null) java.time.Instant.parse(ended);
                    if (!sessionType.equals(type) || (!start && !id.equals(sessionId))) throw new IllegalArgumentException();
                    op.session = new LearningSession(id, type, began, ended);
                    if (!op.session.open()) { callback.completed(new Result(op.session, null, null)); return; }
                    op.request(() -> vocabulary.detail(vocabularyId), 200, detail -> {
                        VocabularyItem card = VocabularyMapper.map(detail);
                        if (!vocabularyId.equals(card.id)) throw new IllegalArgumentException();
                        op.request(() -> vocabulary.examples(vocabularyId), 200, examples -> {
                            VocabularyItem complete = new VocabularyItem(card.id, card.word, card.phoneticIpa,
                                    card.meaningVi, card.meaningEn, card.partOfSpeech, card.cefr, card.topics,
                                    card.audioUrl, VocabularyMapper.mapExamples(examples));
                            callback.completed(new Result(op.session, complete, null));
                        });
                    });
                });
            });
            return op;
        }
    }
    private static String required(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isEmpty()) throw new IllegalArgumentException();
        return value.getAsString();
    }
}
