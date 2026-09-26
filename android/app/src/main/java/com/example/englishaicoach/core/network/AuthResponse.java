package com.example.englishaicoach.core.network;

public final class AuthResponse {
    public String accessToken;
    public String refreshToken;
    public Integer expiresIn;
    public String tokenType;
    public User user;

    public boolean valid() {
        return accessToken != null && !accessToken.trim().isEmpty()
                && refreshToken != null && !refreshToken.trim().isEmpty()
                && expiresIn != null && expiresIn > 0
                && "Bearer".equals(tokenType) && user != null && user.id != null;
    }

    public static final class User {
        public String id;
        public String email;
        public String fullName;
        public String role;
        public String status;
    }
}
