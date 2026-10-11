package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.domain.model.OnboardingData.*;
import com.example.englishaicoach.core.network.ApiError;

public interface OnboardingRepository {
    interface Cancelable { void cancel(); }
    interface Callback { void completed(Result result); }
    final class Result {
        public final Snapshot snapshot;
        public final boolean success, profileSaved;
        public final ApiError error;
        public Result(Snapshot snapshot, boolean success, boolean saved, ApiError error) {
            this.snapshot = snapshot; this.success = success; profileSaved = saved; this.error = error;
        }
    }
    Cancelable load(Callback callback);
    Cancelable save(Profile profile, Selection selection, boolean profileAlreadySaved, Callback callback);
}
