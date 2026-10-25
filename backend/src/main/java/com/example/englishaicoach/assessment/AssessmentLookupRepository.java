package com.example.englishaicoach.assessment;

import com.example.englishaicoach.onboarding.dto.CefrLevelResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Các lookup nội bộ cho application service; không trả entity/catalog ra HTTP. */
@Repository
public class AssessmentLookupRepository {
    private final JdbcTemplate jdbc;

    public AssessmentLookupRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean userExists(UUID userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id=?)", Boolean.class, userId));
    }

    public boolean lockUser(UUID userId) {
        return !jdbc.query("SELECT id FROM users WHERE id=? FOR KEY SHARE",
                (rs, row) -> rs.getObject("id", UUID.class), userId).isEmpty();
    }

    public List<AssessmentVocabulary> catalogSnapshot() {
        return jdbc.query("""
                SELECT v.id,v.word,v.part_of_speech,v.meaning_vi,c.code,v.is_active
                FROM vocabulary v JOIN cefr_levels c ON c.id=v.cefr_level_id
                WHERE v.is_active=TRUE
                """, (rs, row) -> new AssessmentVocabulary(rs.getObject("id", UUID.class),
                        CefrDifficulty.valueOf(rs.getString("code")), rs.getString("word"),
                        rs.getString("part_of_speech"), rs.getString("meaning_vi"), rs.getBoolean("is_active")));
    }

    public CefrLevelResponse level(UUID id) {
        return jdbc.queryForObject("SELECT id,code,name,sort_order FROM cefr_levels WHERE id=?",
                (rs, row) -> new CefrLevelResponse(rs.getObject("id", UUID.class),
                        rs.getString("code"), rs.getString("name"), rs.getInt("sort_order")), id);
    }
}
