package com.example.englishaicoach.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import(PostgreSqlTestContainerConfiguration.class)
public abstract class PostgreSqlIntegrationTestSupport {
}
