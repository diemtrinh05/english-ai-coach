package com.example.englishaicoach.auth;

import java.util.Objects;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final PasswordEncoder longPasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        // BCrypt giới hạn 72 byte; Argon2 giữ đầy đủ password trong contract 100 ký tự.
        return rawPassword.getBytes(StandardCharsets.UTF_8).length > 72
                ? longPasswordEncoder.encode(rawPassword) : encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        Objects.requireNonNull(passwordHash, "passwordHash");
        if (passwordHash.startsWith("$argon2id$")) {
            return longPasswordEncoder.matches(rawPassword, passwordHash);
        }
        return rawPassword.getBytes(StandardCharsets.UTF_8).length <= 72
                && encoder.matches(rawPassword, passwordHash);
    }
}
