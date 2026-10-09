package com.example.englishaicoach.auth;

import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RegistrationRepository {
    private final JdbcTemplate jdbc;

    public RegistrationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean insertLocalUser(UUID id, String email, String passwordHash,
            String fullName, Instant now) {
        // PostgreSQL xử lý email trùng mà không làm hỏng transaction khi có cạnh tranh.
        return jdbc.update("""
                INSERT INTO users (id,email,password_hash,auth_provider,full_name,role,status,
                    failed_login_attempts,created_at,updated_at)
                VALUES (?, ?, ?, 'LOCAL', ?, 'USER', 'ACTIVE', 0, ?, ?)
                ON CONFLICT (email) DO NOTHING
                """, id, email, passwordHash, fullName, java.sql.Timestamp.from(now),
                java.sql.Timestamp.from(now)) == 1;
    }
}
