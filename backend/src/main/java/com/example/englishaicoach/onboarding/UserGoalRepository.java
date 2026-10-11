package com.example.englishaicoach.onboarding;

import com.example.englishaicoach.onboarding.dto.UserGoalResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserGoalRepository {
    private final JdbcTemplate jdbc;
    public UserGoalRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean userExists(UUID userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM users WHERE id=?)", Boolean.class, userId));
    }

    public boolean lockUser(UUID userId) {
        // Khóa hàng user tuần tự hóa cả lần chọn mục tiêu đầu tiên và việc đổi primary.
        return !jdbc.query("SELECT id FROM users WHERE id=? FOR UPDATE", (rs,row) -> rs.getObject("id", UUID.class), userId).isEmpty();
    }

    public List<UserGoalResponse> findByUser(UUID userId) {
        return jdbc.query("""
                SELECT g.id,g.name,ug.is_primary FROM user_goals ug JOIN goals g ON g.id=ug.goal_id
                WHERE ug.user_id=? ORDER BY g.name,g.id
                """, (rs,row) -> new UserGoalResponse(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBoolean("is_primary")), userId);
    }

    public void replace(UUID userId, List<UUID> ids, UUID primaryId, Instant now) {
        // Hạ primary cũ trước insert/update để partial unique luôn được giữ.
        jdbc.update("UPDATE user_goals SET is_primary=false WHERE user_id=? AND is_primary=true", userId);
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        Object[] removalArgs = new Object[ids.size()+1]; removalArgs[0]=userId;
        for(int i=0;i<ids.size();i++) removalArgs[i+1]=ids.get(i);
        jdbc.update("DELETE FROM user_goals WHERE user_id=? AND goal_id NOT IN (" + placeholders + ")", removalArgs);
        for(UUID id:ids) {
            // Giữ id/created_at của liên kết đã có; retry không tạo mutation trùng.
            jdbc.update("""
                    INSERT INTO user_goals(id,user_id,goal_id,is_primary,created_at) VALUES (?,?,?,false,?)
                    ON CONFLICT(user_id,goal_id) DO NOTHING
                    """, UUID.randomUUID(), userId, id, Timestamp.from(now));
        }
        if(primaryId!=null) jdbc.update("UPDATE user_goals SET is_primary=true WHERE user_id=? AND goal_id=?",userId,primaryId);
    }
}
