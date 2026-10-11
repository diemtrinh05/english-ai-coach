package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.OnboardingApi;
import com.example.englishaicoach.data.dto.OnboardingDtos;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import com.example.englishaicoach.domain.repository.OnboardingRepository;
import com.google.gson.JsonElement;
import java.util.*;
import java.io.IOException;
import java.util.function.*;
import retrofit2.*;

public final class RetrofitOnboardingRepository implements OnboardingRepository {
    private final SessionManager sessions;
    private final OnboardingApi api;
    private final ApiErrorParser errors;
    public RetrofitOnboardingRepository(SessionManager sessions, OnboardingApi api, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.errors = errors;
    }
    private final class Operation implements Cancelable {
        final String sessionId;
        final Callback callback;
        Call<JsonElement> active;
        boolean canceled, profileSaved;
        Operation(Callback callback) {
            TokenSession session = sessions.current(); sessionId = session == null ? null : session.sessionId(); this.callback = callback;
        }
        boolean current() { return !canceled && sessionId != null && sessions.isCurrentSession(sessionId); }
        @Override public void cancel() { synchronized (sessions) { canceled = true; if (active != null) active.cancel(); } }
        void result(Result result) { if (current()) callback.completed(result); }
        void request(Supplier<Call<JsonElement>> factory, Consumer<Response<JsonElement>> next) {
            synchronized (sessions) {
                if (!current()) return;
                // Khởi tạo từng stage trong cùng lock phiên để không gửi dữ liệu A dưới phiên B.
                active = factory.get();
                active.enqueue(new retrofit2.Callback<JsonElement>() {
                    @Override public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        synchronized (sessions) {
                            try {
                                if (!current() || call.isCanceled()) return;
                                next.accept(response);
                            } catch (RuntimeException malformed) {
                                result(new Result(null, false, profileSaved, errors.parse(502, null)));
                            } finally { if (response.errorBody() != null) response.errorBody().close(); }
                        }
                    }
                    @Override public void onFailure(Call<JsonElement> call, Throwable failure) {
                        synchronized (sessions) {
                            if (current() && !call.isCanceled()) result(new Result(null, false, profileSaved,
                                    errors.networkFailure(new IOException("Network request failed"))));
                        }
                    }
                });
            }
        }
        boolean ok(Response<JsonElement> response) {
            if (!response.isSuccessful()) {
                result(new Result(null, false, profileSaved, errors.parse(response))); return false;
            }
            if (response.body() == null) throw new IllegalStateException("Missing response");
            return true;
        }
    }
    @Override public Cancelable load(Callback callback) {
        synchronized (sessions) {
            Operation op = new Operation(callback);
            op.request(api::user, user -> {
                if (!op.ok(user)) return;
                String name = OnboardingDtos.string(user.body().getAsJsonObject(), "fullName");
                op.request(api::profile, profile -> {
                    Profile initial;
                    if (profile.code() == 404) initial = new Profile(name, null, null, null);
                    else { if (!op.ok(profile)) return; initial = OnboardingDtos.profile(profile.body()); }
                    op.request(api::catalog, catalog -> {
                        if (!op.ok(catalog)) return;
                        List<Goal> goals = OnboardingDtos.catalog(catalog.body());
                        op.request(api::selection, selected -> {
                            if (!op.ok(selected)) return;
                            Selection selection = OnboardingDtos.selection(selected.body(), goals);
                            op.result(new Result(new Snapshot(initial, goals, selection), true, false, null));
                        });
                    });
                });
            });
            return op;
        }
    }
    @Override public Cancelable save(Profile profile, Selection selection, boolean saved, Callback callback) {
        synchronized (sessions) {
            Operation op = new Operation(callback); op.profileSaved = saved;
            Runnable saveGoals = () -> op.request(() -> api.saveSelection(selection), response -> {
                if (!op.ok(response)) return;
                Selection actual = OnboardingDtos.selection(response.body(), new ArrayList<>());
                if (!new HashSet<>(actual.goalIds).equals(new HashSet<>(selection.goalIds))
                        || !Objects.equals(actual.primaryGoalId, selection.primaryGoalId)) throw new IllegalStateException("Invalid selection result");
                op.result(new Result(null, true, true, null));
            });
            if (saved) saveGoals.run();
            else op.request(() -> api.saveProfile(profile), response -> {
                if (!op.ok(response)) return;
                if (!profile.equals(OnboardingDtos.profile(response.body()))) throw new IllegalStateException("Invalid profile result");
                op.profileSaved = true; saveGoals.run();
            });
            return op;
        }
    }
}
