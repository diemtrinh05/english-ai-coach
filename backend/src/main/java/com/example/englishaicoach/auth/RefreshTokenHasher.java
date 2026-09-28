package com.example.englishaicoach.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {

    public String hash(String rawToken) {
        Objects.requireNonNull(rawToken, "rawToken");
        if (rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token không được để trống");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Không có SHA-256", exception);
        }
    }
}
