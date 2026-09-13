package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.englishaicoach.common.persistence.AuditableEntity;
import com.example.englishaicoach.common.persistence.UuidEntity;
import com.example.englishaicoach.gamification.Streak;
import com.example.englishaicoach.learning.LearningStatus;
import com.example.englishaicoach.learning.UserVocabularyProgress;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.junit.jupiter.api.Test;

class JpaEntityConventionTests {

    @Test
    void uuidBaseUsesGeneratedUuidIdentifiers() throws NoSuchFieldException {
        Field id = UuidEntity.class.getDeclaredField("id");
        GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);

        assertEquals(UUID.class, id.getType());
        assertNotNull(generatedValue);
        assertEquals(GenerationType.UUID, generatedValue.strategy());
        assertEquals("id", id.getAnnotation(Column.class).name());
    }

    @Test
    void auditTimestampsUseInstantForTimestampWithTimeZone() throws NoSuchFieldException {
        Field createdAt = AuditableEntity.class.getDeclaredField("createdAt");
        Field updatedAt = AuditableEntity.class.getDeclaredField("updatedAt");

        assertEquals(Instant.class, createdAt.getType());
        assertEquals(Instant.class, updatedAt.getType());
        assertNotNull(createdAt.getAnnotation(CreationTimestamp.class));
        assertNotNull(updatedAt.getAnnotation(UpdateTimestamp.class));
        assertEquals("created_at", createdAt.getAnnotation(Column.class).name());
        assertEquals("updated_at", updatedAt.getAnnotation(Column.class).name());
    }

    @Test
    void userVocabularyProgressUsesStringEnumAndOptimisticLocking() throws NoSuchFieldException {
        Table table = UserVocabularyProgress.class.getAnnotation(Table.class);
        Field status = UserVocabularyProgress.class.getDeclaredField("status");
        Field version = UserVocabularyProgress.class.getDeclaredField("version");

        assertEquals("user_vocabulary_progress", table.name());
        assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
        assertEquals(LearningStatus.class, status.getType());
        assertNotNull(version.getAnnotation(Version.class));
        assertEquals(Long.class, version.getType());
        assertEquals(Set.of("NEW", "LEARNING", "REVIEWING", "MASTERED"),
                Arrays.stream(LearningStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet()));
    }

    @Test
    void streakUsesDateAndOptimisticLockingConventions() throws NoSuchFieldException {
        Table table = Streak.class.getAnnotation(Table.class);
        Field lastActiveDate = Streak.class.getDeclaredField("lastActiveDate");
        Field updatedAt = Streak.class.getDeclaredField("updatedAt");
        Field version = Streak.class.getDeclaredField("version");

        assertEquals("streaks", table.name());
        assertEquals(LocalDate.class, lastActiveDate.getType());
        assertEquals(Instant.class, updatedAt.getType());
        assertNotNull(updatedAt.getAnnotation(UpdateTimestamp.class));
        assertNotNull(version.getAnnotation(Version.class));
        assertEquals(Long.class, version.getType());
    }

    @Test
    void mappedColumnsMatchTheCanonicalOptimisticLockTables() {
        assertEquals(Set.of(
                        "user_id", "vocabulary_id", "status", "ease_factor", "interval_days",
                        "repetitions", "next_review_at", "last_reviewed_at", "correct_count",
                        "incorrect_count", "avg_response_time_ms", "last_quality", "version"),
                declaredColumnNames(UserVocabularyProgress.class));
        assertEquals(Set.of(
                        "user_id", "current_streak", "longest_streak", "last_active_date",
                        "version", "updated_at"),
                declaredColumnNames(Streak.class));
    }

    private Set<String> declaredColumnNames(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(field -> field.getAnnotation(Column.class).name())
                .collect(Collectors.toSet());
    }
}
