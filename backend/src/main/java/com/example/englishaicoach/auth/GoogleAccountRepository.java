package com.example.englishaicoach.auth;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GoogleAccountRepository {
    private final JdbcTemplate jdbc;

    public GoogleAccountRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Account> findLocked(String subject) {
        return jdbc.query("""
                SELECT id,email,full_name,role,status FROM users
                WHERE auth_provider='GOOGLE' AND provider_user_id=? FOR UPDATE
                """, (rs, row) -> new Account(rs.getObject("id", UUID.class), rs.getString("email"),
                        rs.getString("full_name"), UserRole.valueOf(rs.getString("role")),
                        UserStatus.valueOf(rs.getString("status"))), subject).stream().findFirst();
    }

    public void insertIfFree(VerifiedGoogleIdentity identity, Instant now) {
        // Xử lý cả unique email và unique provider/sub mà không gây failed transaction.
        jdbc.update("""
                INSERT INTO users(id,email,password_hash,auth_provider,provider_user_id,full_name,
                    role,status,failed_login_attempts,created_at,updated_at)
                VALUES (?, ?, NULL, 'GOOGLE', ?, ?, 'USER', 'ACTIVE', 0, ?, ?)
                ON CONFLICT DO NOTHING
                """, UUID.randomUUID(), identity.email(), identity.subject(), identity.name(),
                Timestamp.from(now), Timestamp.from(now));
    }

    public void recordLogin(UUID id, Instant now) {
        // Google login không đổi counters/cooldown của password login.
        jdbc.update("UPDATE users SET last_login_at=?,updated_at=? WHERE id=?",
                Timestamp.from(now), Timestamp.from(now), id);
    }

    public record Account(UUID id, String email, String fullName, UserRole role, UserStatus status) { }
}
