package com.example.englishaicoach.user;

import com.example.englishaicoach.auth.UserRole;
import com.example.englishaicoach.auth.UserStatus;
import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.onboarding.dto.CefrLevelResponse;
import com.example.englishaicoach.user.dto.UpdateProfileRequest;
import com.example.englishaicoach.user.dto.UserProfileResponse;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileRepository {
    private final JdbcTemplate jdbc;
    public ProfileRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<AuthUserSummary> findUser(UUID userId) {
        return jdbc.query("SELECT id,email,full_name,role,status FROM users WHERE id=?",
                (rs, row) -> new AuthUserSummary(rs.getObject("id", UUID.class), rs.getString("email"),
                        rs.getString("full_name"), UserRole.valueOf(rs.getString("role")),
                        UserStatus.valueOf(rs.getString("status"))), userId).stream().findFirst();
    }

    public Optional<UserProfileResponse> findProfile(UUID userId) {
        return jdbc.query("""
                SELECT u.full_name,p.avatar_url,p.daily_learning_minutes,p.timezone,
                       c.id AS cefr_id,c.code,c.name,c.sort_order
                FROM user_profiles p JOIN users u ON u.id=p.user_id
                LEFT JOIN cefr_levels c ON c.id=p.current_cefr_level_id
                WHERE p.user_id=?
                """, (rs, row) -> new UserProfileResponse(rs.getString("full_name"), rs.getString("avatar_url"),
                        rs.getObject("cefr_id", UUID.class) == null ? null : new CefrLevelResponse(
                                rs.getObject("cefr_id", UUID.class), rs.getString("code"), rs.getString("name"),
                                rs.getInt("sort_order")), rs.getInt("daily_learning_minutes"), rs.getString("timezone")),
                userId).stream().findFirst();
    }

    public boolean lockUser(UUID userId) {
        // Khóa hàng user để tuần tự hóa cả lần tạo profile đầu tiên và cập nhật tên.
        return !jdbc.query("SELECT id FROM users WHERE id=? FOR UPDATE",
                (rs, row) -> rs.getObject("id", UUID.class), userId).isEmpty();
    }

    public void update(UUID userId, UpdateProfileRequest request, Instant now) {
        Timestamp timestamp = Timestamp.from(now);
        jdbc.update("UPDATE users SET full_name=?,updated_at=? WHERE id=?", request.fullName(), timestamp, userId);
        // Chỉ ghi các trường được phép; CEFR và created_at đã tồn tại được bảo toàn.
        jdbc.update("""
                INSERT INTO user_profiles (id,user_id,avatar_url,daily_learning_minutes,timezone,created_at,updated_at)
                VALUES (?,?,?,?,?,?,?)
                ON CONFLICT (user_id) DO UPDATE SET avatar_url=EXCLUDED.avatar_url,
                    daily_learning_minutes=EXCLUDED.daily_learning_minutes,timezone=EXCLUDED.timezone,
                    updated_at=EXCLUDED.updated_at
                """, UUID.randomUUID(), userId, request.avatarUrl(), request.dailyLearningMinutes(),
                request.timezone(), timestamp, timestamp);
    }
}
