package com.example.englishaicoach.core.network;

public final class TokenRefreshResponse {
    public String accessToken;
    public String refreshToken;
    public Integer expiresIn;
    public String tokenType;

    public boolean valid() {
        return accessToken != null && !accessToken.trim().isEmpty()
                && refreshToken != null && !refreshToken.trim().isEmpty()
                && "Bearer".equals(tokenType) && expiresIn != null && expiresIn > 0;
    }
}
