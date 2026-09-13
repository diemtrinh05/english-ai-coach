package com.example.englishaicoach.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSqlTestContainerConfiguration {

    private static final DockerImageName POSTGRES_IMAGE =
            DockerImageName.parse("postgres:16.15-alpine3.24");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSqlContainer() {
        return new PostgreSQLContainer(POSTGRES_IMAGE)
                .withDatabaseName("english_ai_coach_test")
                .withUsername("english_ai_coach_test")
                .withPassword("english_ai_coach_test");
    }
}
