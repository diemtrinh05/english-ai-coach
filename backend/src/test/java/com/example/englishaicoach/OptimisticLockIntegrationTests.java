package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.englishaicoach.gamification.Streak;
import com.example.englishaicoach.support.PostgreSqlIntegrationTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class OptimisticLockIntegrationTests extends PostgreSqlIntegrationTestSupport {

    private final UUID userId = UUID.randomUUID();
    private final UUID streakId = UUID.randomUUID();

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM streaks WHERE id = ?", streakId);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }

    @Test
    void staleVersionRaisesOptimisticLockExceptionWithoutSilentOverwrite() throws Exception {
        insertFixture();

        EntityManager winnerManager = entityManagerFactory.createEntityManager();
        EntityManager loserManager = entityManagerFactory.createEntityManager();
        try {
            winnerManager.getTransaction().begin();
            loserManager.getTransaction().begin();

            Streak winner = winnerManager.find(Streak.class, streakId);
            Streak loser = loserManager.find(Streak.class, streakId);
            assertNotNull(winner);
            assertNotNull(loser);
            assertEquals(0L, winner.getVersion());
            assertEquals(0L, loser.getVersion());

            setCurrentStreak(winner, 2);
            setCurrentStreak(loser, 99);

            winnerManager.getTransaction().commit();

            RuntimeException conflict = assertThrows(
                    RuntimeException.class,
                    () -> loserManager.getTransaction().commit());
            assertTrue(hasCause(conflict, OptimisticLockException.class));
        } finally {
            rollbackIfActive(winnerManager);
            rollbackIfActive(loserManager);
            winnerManager.close();
            loserManager.close();
        }

        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT current_streak FROM streaks WHERE id = ?",
                Integer.class,
                streakId));
        assertEquals(1L, jdbcTemplate.queryForObject(
                "SELECT version FROM streaks WHERE id = ?",
                Long.class,
                streakId));
    }

    private void insertFixture() {
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id, email, auth_provider, full_name, role, status,
                    failed_login_attempts, created_at, updated_at
                ) VALUES (?, ?, 'LOCAL', 'Optimistic Lock Test', 'USER', 'ACTIVE', 0, ?, ?)
                """,
                userId,
                "optimistic-lock-" + userId + "@example.com",
                Timestamp.from(now),
                Timestamp.from(now));
        jdbcTemplate.update(
                """
                INSERT INTO streaks (
                    id, user_id, current_streak, longest_streak, last_active_date,
                    version, updated_at
                ) VALUES (?, ?, 1, 1, CURRENT_DATE, 0, ?)
                """,
                streakId,
                userId,
                Timestamp.from(now));
    }

    private void setCurrentStreak(Streak streak, int value) throws Exception {
        Field field = Streak.class.getDeclaredField("currentStreak");
        field.setAccessible(true);
        field.set(streak, value);
    }

    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        Throwable current = error;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void rollbackIfActive(EntityManager entityManager) {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }
}
