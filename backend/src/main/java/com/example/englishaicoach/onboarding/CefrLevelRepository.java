package com.example.englishaicoach.onboarding;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CefrLevelRepository {

    private final JdbcTemplate jdbcTemplate;

    public CefrLevelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CefrLevel> findAllInCanonicalOrder() {
        return jdbcTemplate.query("""
                SELECT id, code, name, sort_order
                FROM cefr_levels
                ORDER BY sort_order, code
                """, (resultSet, rowNumber) -> new CefrLevel(
                resultSet.getObject("id", java.util.UUID.class),
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getInt("sort_order")));
    }
}
