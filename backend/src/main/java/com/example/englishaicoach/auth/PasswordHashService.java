package com.example.englishaicoach.auth;

import java.util.Objects;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        Objects.requireNonNull(passwordHash, "passwordHash");
        return encoder.matches(rawPassword, passwordHash);
    }
}
