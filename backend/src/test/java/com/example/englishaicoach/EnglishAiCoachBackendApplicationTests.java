package com.example.englishaicoach;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.englishaicoach.auth.RefreshTokenRepository;
import com.example.englishaicoach.auth.UserRepository;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "management.endpoint.health.group.readiness.include=readinessState"
})
class EnglishAiCoachBackendApplicationTests {

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }
}
