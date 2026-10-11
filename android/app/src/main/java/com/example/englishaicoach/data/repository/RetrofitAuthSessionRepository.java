package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.auth.TokenSession;
import com.example.englishaicoach.data.api.CurrentUserApi;
import com.example.englishaicoach.data.dto.CurrentUserDto;
import com.example.englishaicoach.domain.repository.AuthSessionRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class RetrofitAuthSessionRepository implements AuthSessionRepository {
    private final SessionManager sessions;
    private final CurrentUserApi api;
    public RetrofitAuthSessionRepository(SessionManager sessions, CurrentUserApi api) {
        this.sessions = sessions;
        this.api = api;
    }
    @Override public Cancelable restore(AuthSessionRepository.Callback callback) {
        synchronized (sessions) {
            return restoreCurrent(callback);
        }
    }
    private Cancelable restoreCurrent(AuthSessionRepository.Callback callback) {
        TokenSession snapshot = sessions.current();
        if (snapshot == null) {
            callback.completed(Result.UNAUTHENTICATED);
            return () -> { };
        }
        Call<CurrentUserDto> call = api.currentUser();
        call.enqueue(new retrofit2.Callback<CurrentUserDto>() {
            @Override public void onResponse(Call<CurrentUserDto> call, Response<CurrentUserDto> response) {
                if (call.isCanceled()) {
                    if (response.errorBody() != null) response.errorBody().close();
                    return;
                }
                // Callback cũ không điều hướng hoặc xóa thông tin xác thực của tài khoản mới.
                TokenSession current = sessions.current();
                if (current != null && !current.sessionId().equals(snapshot.sessionId())) {
                    if (response.errorBody() != null) response.errorBody().close();
                    return;
                }
                if (response.code() == 401) {
                    callback.completed(sessions.current() == null ? Result.SESSION_EXPIRED : Result.ERROR);
                } else if (current != null && response.isSuccessful() && response.body() != null
                        && response.body().valid()) callback.completed(Result.AUTHENTICATED);
                else callback.completed(current == null ? Result.SESSION_EXPIRED : Result.ERROR);
                if (response.errorBody() != null) response.errorBody().close();
            }
            @Override public void onFailure(Call<CurrentUserDto> call, Throwable failure) {
                if (call.isCanceled()) return;
                TokenSession current = sessions.current();
                if (current != null && !current.sessionId().equals(snapshot.sessionId())) return;
                callback.completed(current == null ? Result.SESSION_EXPIRED : Result.ERROR);
            }
        });
        return call::cancel;
    }
}
