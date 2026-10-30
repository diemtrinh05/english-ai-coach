package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.data.api.OnboardingApi;
import com.example.englishaicoach.data.dto.CurrentUserDto;
import com.example.englishaicoach.data.dto.OnboardingDtos;
import com.example.englishaicoach.domain.model.OnboardingRoute;
import com.example.englishaicoach.domain.repository.OnboardingGateRepository;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import retrofit2.*;

public final class RetrofitOnboardingGateRepository implements OnboardingGateRepository {
    private final SessionManager sessions;
    private final OnboardingApi api;
    private final ApiErrorParser errors;
    public RetrofitOnboardingGateRepository(SessionManager sessions, OnboardingApi api, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.errors = errors;
    }
    private final class Operation implements Cancelable {
        final String owner;
        final OnboardingGateRepository.Callback callback;
        Call<JsonElement> active;
        boolean canceled;
        Operation(String owner, OnboardingGateRepository.Callback callback) { this.owner = owner; this.callback = callback; }
        boolean current() { return !canceled && sessions.isCurrentSession(owner); }
        public void cancel() { synchronized (sessions) { canceled = true; if (active != null) active.cancel(); } }
        void request(Supplier<Call<JsonElement>> factory, Consumer<Response<JsonElement>> next) {
            synchronized (sessions) {
                if (!current()) return;
                active = factory.get();
                active.enqueue(new retrofit2.Callback<JsonElement>() {
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        synchronized (sessions) {
                            try { if (current() && !call.isCanceled()) next.accept(response); }
                            catch (RuntimeException malformed) { if (current()) callback.completed(null, errors.parse(502, null)); }
                            finally { if (response.errorBody() != null) response.errorBody().close(); }
                        }
                    }
                    public void onFailure(Call<JsonElement> call, Throwable failure) {
                        synchronized (sessions) { if (current() && !call.isCanceled()) callback.completed(null, errors.parse(0, null)); }
                    }
                });
            }
        }
        boolean ok(Response<JsonElement> response) {
            if (response.code() != 200) { callback.completed(null, errors.parse(response)); return false; }
            if (response.body() == null || !response.body().isJsonObject()) throw new JsonParseException("Invalid response");
            return true;
        }
    }
    public Cancelable resolve(String owner, OnboardingGateRepository.Callback callback) {
        Operation op = new Operation(owner, callback);
        op.request(api::user, user -> {
            if (!op.ok(user)) return;
            CurrentUserDto identity = new Gson().fromJson(user.body(), CurrentUserDto.class);
            if (identity == null || !identity.valid() || !"USER".equals(identity.role)) {
                callback.completed(null, errors.parse(403, null)); return;
            }
            op.request(api::profile, profile -> {
                if (profile.code() == 404) { callback.completed(OnboardingRoute.SETTINGS, null); return; }
                if (!op.ok(profile)) return;
                OnboardingDtos.profile(profile.body());
                JsonObject data = profile.body().getAsJsonObject();
                boolean hasLevel = data.has("currentCefrLevel") && !data.get("currentCefrLevel").isJsonNull();
                if (hasLevel) AssessmentResponseParser.level(data.get("currentCefrLevel"));
                op.request(api::selection, goals -> {
                    if (!op.ok(goals)) return;
                    boolean selected = !OnboardingDtos.selection(goals.body(), new ArrayList<>()).goalIds.isEmpty();
                    callback.completed(!selected ? OnboardingRoute.SETTINGS : hasLevel ? OnboardingRoute.HOME : OnboardingRoute.PLACEMENT, null);
                });
            });
        });
        return op;
    }
}
