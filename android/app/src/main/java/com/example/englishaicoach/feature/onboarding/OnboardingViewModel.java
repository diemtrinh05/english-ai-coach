package com.example.englishaicoach.feature.onboarding;

import androidx.lifecycle.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import com.example.englishaicoach.domain.repository.OnboardingRepository;
import com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase;
import java.util.*;
import java.util.concurrent.Executor;

public final class OnboardingViewModel extends ViewModel {
    public enum Phase { INITIAL, LOADING, EDITING, SAVING, SUCCESS, ERROR, EXPIRED }
    public static final class State {
        public final Phase phase;
        public final Snapshot snapshot;
        public final OnboardingRepository.Result result;
        public final boolean validation;
        State(Phase phase, Snapshot snapshot, OnboardingRepository.Result result, boolean validation) {
            this.phase = phase; this.snapshot = snapshot; this.result = result; this.validation = validation;
        }
    }
    private final ConfigureOnboardingUseCase useCase;
    private final SessionManager sessions;
    private final Executor executor;
    private final String sessionId;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Phase.INITIAL, null, null, false));
    private final SessionManager.Listener listener;
    private OnboardingRepository.Cancelable active;
    private long generation;
    private boolean closed;
    private Profile savedProfile;
    public String name = "", zone = "", minutes = "";
    public boolean zoneConfirmed;
    public final Set<String> selected = new LinkedHashSet<>();
    public String primary;
    public int step;
    public OnboardingViewModel(ConfigureOnboardingUseCase useCase, SessionManager sessions, Executor executor) {
        this.useCase = useCase; this.sessions = sessions; this.executor = executor;
        TokenSession initial = sessions.current(); sessionId = initial == null ? null : initial.sessionId();
        listener = () -> executor.execute(() -> {
            if (!closed && !current()) {
                generation++; if (active != null) active.cancel();
                name = ""; zone = ""; minutes = ""; zoneConfirmed = false; selected.clear(); primary = null; savedProfile = null;
                state.setValue(new State(Phase.EXPIRED, null, null, false));
            }
        });
        sessions.addListener(listener);
    }
    public LiveData<State> state() { return state; }
    private boolean current() { return sessionId != null && sessions.isCurrentSession(sessionId); }
    public void load() {
        State old = state.getValue();
        if (closed || !current() || old.phase != Phase.INITIAL && old.phase != Phase.ERROR) return;
        long request = ++generation;
        state.setValue(new State(Phase.LOADING, null, null, false));
        active = useCase.load(result -> executor.execute(() -> {
            if (closed || !current() || request != generation) return;
            if (result.success) {
                Snapshot snapshot = result.snapshot;
                name = snapshot.profile.fullName; zone = snapshot.profile.timezone == null ? "" : snapshot.profile.timezone;
                minutes = snapshot.profile.dailyLearningMinutes == null ? "" : snapshot.profile.dailyLearningMinutes.toString();
                zoneConfirmed = snapshot.profile.timezone != null;
                selected.clear(); selected.addAll(snapshot.selection.goalIds); primary = snapshot.selection.primaryGoalId;
                savedProfile = null; step = 0;
                state.setValue(new State(Phase.EDITING, snapshot, null, false));
            } else state.setValue(new State(Phase.ERROR, null, result, false));
        }));
    }
    public boolean validProfileFields() {
        return name != null && !name.trim().isEmpty() && name.length() <= 100
                && zone != null && !zone.isEmpty() && zone.length() <= 50 && zoneConfirmed;
    }
    public boolean validGoals() {
        Snapshot snapshot = state.getValue().snapshot;
        return snapshot != null && !selected.isEmpty() && (primary == null || selected.contains(primary))
                && selected.stream().allMatch(id -> snapshot.goals.stream().anyMatch(goal -> goal.id.equals(id) && goal.active));
    }
    public Integer parsedMinutes() {
        try { if (!minutes.matches("[0-9]+")) return null; int value = Integer.parseInt(minutes); return value >= 5 && value <= 180 ? value : null; }
        catch (RuntimeException invalid) { return null; }
    }
    public void save(boolean online) {
        State old = state.getValue();
        if (closed || !current() || old.phase == Phase.SAVING || old.snapshot == null) return;
        if (!validProfileFields() || !validGoals() || parsedMinutes() == null) {
            state.setValue(new State(Phase.EDITING, old.snapshot, old.result, true)); return;
        }
        if (!online) {
            state.setValue(new State(Phase.EDITING, old.snapshot,
                    new OnboardingRepository.Result(null, false, savedProfile != null,
                    new com.example.englishaicoach.core.network.ApiErrorParser().parse(0, null)), false)); return;
        }
        Profile profile = new Profile(name, parsedMinutes(), zone, old.snapshot.profile.avatarUrl);
        Selection selection = new Selection(selected, primary);
        boolean skipProfile = profile.equals(savedProfile);
        long request = ++generation;
        state.setValue(new State(Phase.SAVING, old.snapshot, old.result, false));
        active = useCase.save(profile, selection, skipProfile, result -> executor.execute(() -> {
            if (closed || !current() || request != generation) return;
            if (result.profileSaved) savedProfile = profile;
            state.setValue(new State(result.success ? Phase.SUCCESS : Phase.EDITING, old.snapshot, result, false));
        }));
    }
    public void publishDraft() {
        State old = state.getValue();
        if (old.phase == Phase.EDITING) state.setValue(new State(Phase.EDITING, old.snapshot, old.result, false));
    }
    @Override protected void onCleared() {
        closed = true; generation++; if (active != null) active.cancel(); sessions.removeListener(listener);
        name = ""; zone = ""; minutes = ""; selected.clear(); primary = null; savedProfile = null;
    }
}
