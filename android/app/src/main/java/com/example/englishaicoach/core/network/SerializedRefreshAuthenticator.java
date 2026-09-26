package com.example.englishaicoach.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.auth.TokenSession;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;
import retrofit2.Call;

public final class SerializedRefreshAuthenticator implements Authenticator {
    private final SessionManager sessions;
    private final RefreshService refreshService;
    private final Gson gson = new Gson();
    private final Object refreshLock = new Object();

    public SerializedRefreshAuthenticator(SessionManager sessions, RefreshService refreshService) {
        this.sessions = sessions;
        this.refreshService = refreshService;
    }

    @Nullable @Override public Request authenticate(@Nullable Route route, @NonNull Response response) {
        if (responseCount(response) > 1 || isPublicAuth(response.request())) {
            return null;
        }
        TokenSession bound = response.request().tag(TokenSession.class);
        if (bound == null) return null;
        synchronized (refreshLock) {
            TokenSession current = sessions.current();
            if (current == null || !current.sessionId().equals(bound.sessionId())) return null;

            // Một request khác đã refresh cùng phiên; dùng token mới mà không gọi refresh lần nữa.
            if (!current.accessToken().equals(bound.accessToken())) {
                return retry(response.request(), current);
            }
            retrofit2.Response<ResponseBody> refreshed;
            try {
                Call<ResponseBody> call = refreshService.refresh(new RefreshRequest(current.refreshToken()));
                refreshed = call.execute();
            } catch (IOException failure) {
                // Lỗi mạng không chứng minh refresh token hết hạn; giữ phiên để thử lại về sau.
                return null;
            } catch (RuntimeException failure) {
                // Lỗi khởi tạo/gửi refresh không hợp lệ không được thoát khỏi OkHttp Authenticator.
                sessions.clearIfCurrent(current);
                return null;
            }
            if (!refreshed.isSuccessful()) {
                ResponseBody errorBody = refreshed.errorBody();
                if (errorBody != null) errorBody.close();
                // Lỗi 408/429/5xx có thể hồi phục; lỗi hợp đồng/xác thực khác kết thúc phiên.
                if (!isTransientStatus(refreshed.code())) sessions.clearIfCurrent(current);
                return null;
            }
            TokenRefreshResponse body;
            try (ResponseBody raw = refreshed.body()) {
                if (raw == null) {
                    sessions.clearIfCurrent(current);
                    return null;
                }
                body = gson.fromJson(raw.string(), TokenRefreshResponse.class);
                if (body == null || !body.valid()) {
                    sessions.clearIfCurrent(current);
                    return null;
                }
            } catch (IOException | RuntimeException malformedResponse) {
                // Body 200 không parse được là phản hồi không hợp lệ, khác lỗi kết nối tạm thời.
                sessions.clearIfCurrent(current);
                return null;
            }
            if (!sessions.updateAccessIfCurrent(current, body.accessToken)) return null;
            TokenSession updated = sessions.current();
            return updated == null ? null : retry(response.request(), updated);
        }
    }

    private static Request retry(Request original, TokenSession current) {
        return original.newBuilder()
                .tag(TokenSession.class, current)
                .header("Authorization", "Bearer " + current.accessToken())
                .build();
    }

    private static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) count++;
        return count;
    }

    private static boolean isTransientStatus(int status) {
        return status == 408 || status == 429 || status >= 500 && status <= 599;
    }

    private static boolean isPublicAuth(Request request) {
        String path = request.url().encodedPath();
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/google")
                || path.equals("/api/v1/auth/refresh");
    }
}
