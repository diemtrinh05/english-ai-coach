package com.example.englishaicoach.onboarding;

import com.example.englishaicoach.onboarding.dto.GoalResponse;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GoalRepository {
    private final JdbcTemplate jdbc;
    public GoalRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<GoalResponse> findActive() {
        return jdbc.query("SELECT id,name,description FROM goals WHERE is_active=true ORDER BY name,id",
                (rs,row) -> new GoalResponse(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("description")));
    }

    public List<Selection> lockSelections(List<UUID> ids) {
        // Khóa chia sẻ theo thứ tự cố định để trạng thái active không đổi trước commit.
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT id,is_active FROM goals WHERE id IN (" + placeholders + ") ORDER BY id FOR SHARE",
                (rs,row) -> new Selection(rs.getObject("id", UUID.class), rs.getBoolean("is_active")), ids.toArray());
    }

    public record Selection(UUID id, boolean active) {}
}
