package com.example.englishaicoach.data.repository;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.ApiError;
import com.example.englishaicoach.core.network.ApiErrorParser;
import com.example.englishaicoach.data.api.AuthFormsApi;
import com.example.englishaicoach.data.dto.AuthFormDtos;
import com.example.englishaicoach.domain.repository.AuthFormsRepository;
import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitAuthFormsRepository implements AuthFormsRepository {
    private final SessionManager sessions;
    private final AuthFormsApi api;
    private final ApiErrorParser errors;
    public RetrofitAuthFormsRepository(SessionManager sessions, AuthFormsApi api, ApiErrorParser errors) {
        this.sessions = sessions; this.api = api; this.errors = errors;
    }
    @Override public Cancelable submit(boolean register, String fullName, String email, String password,
            AuthFormsRepository.Callback callback) {
        long expected = sessions.generation();
        Call<AuthFormDtos.Response> call = register
                ? api.register(new AuthFormDtos.Register(fullName, email, password))
                : api.login(new AuthFormDtos.Login(email, password));
        java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean();
        call.enqueue(new retrofit2.Callback<AuthFormDtos.Response>() {
            @Override public void onResponse(Call<AuthFormDtos.Response> request, Response<AuthFormDtos.Response> response) {
                synchronized (sessions) {
                    try {
                        if (cancelled.get() || request.isCanceled()) return;
                        if (expected != sessions.generation()) { callback.completed(Result.SUPERSEDED); return; }
                        AuthFormDtos.Response body = response.body();
                        if (response.code() == (register ? 201 : 200) && body != null && body.valid()) {
                            // Linearize callback với logout/account switch; không khôi phục phiên từ response cũ.
                            sessions.startIfUnchanged(expected, body.accessToken, body.refreshToken);
                            callback.completed(Result.SUCCESS);
                        } else if (response.isSuccessful()) callback.completed(Result.ERROR);
                        else callback.completed(map(errors.parse(response)));
                    } catch (RuntimeException storageFailure) {
                        callback.completed(Result.ERROR);
                    } finally { if (response.errorBody() != null) response.errorBody().close(); }
                }
            }
            @Override public void onFailure(Call<AuthFormDtos.Response> request, Throwable failure) {
                synchronized (sessions) {
                    if (!cancelled.get() && !request.isCanceled()) callback.completed(expected == sessions.generation()
                            ? Result.NETWORK : Result.SUPERSEDED);
                }
            }
        });
        return () -> { synchronized (sessions) { cancelled.set(true); call.cancel(); } };
    }
    private static Result map(ApiError error) {
        if (error.status == 401 && "AUTH_INVALID_CREDENTIALS".equals(error.code)) return Result.INVALID_CREDENTIALS;
        if (error.status == 423 && "AUTH_ACCOUNT_LOCKED".equals(error.code)) return Result.LOCKED;
        if (error.status == 409 && "CONFLICT".equals(error.code)) return Result.EMAIL_EXISTS;
        if (error.status == 400) return Result.VALIDATION;
        if (error.status == 429) return Result.RATE_LIMITED;
        return Result.ERROR;
    }
}
