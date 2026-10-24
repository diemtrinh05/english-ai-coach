package com.example.englishaicoach;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.englishaicoach.auth.RefreshTokenRepository;
import com.example.englishaicoach.auth.UserRepository;
import com.example.englishaicoach.gamification.StreakRepository;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "management.endpoint.health.group.readiness.include=readinessState"
})
class EnglishAiCoachBackendApplicationTests {

    @MockitoBean
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StreakRepository streakRepository;

    @MockitoBean
    private com.example.englishaicoach.learning.LearningAttemptRepository learningAttemptRepository;

    @MockitoBean
    private com.example.englishaicoach.assessment.UserLevelAssessmentRepository userLevelAssessmentRepository;

    @MockitoBean
    private com.example.englishaicoach.assessment.AssessmentItemRepository assessmentItemRepository;

    @Test
    void contextLoads() {
    }
}
