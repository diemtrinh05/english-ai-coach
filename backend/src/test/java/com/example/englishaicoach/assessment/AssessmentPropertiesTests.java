package com.example.englishaicoach.assessment;

import static org.junit.jupiter.api.Assertions.*;

import com.example.englishaicoach.config.AssessmentProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AssessmentPropertiesTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=test").withUserConfiguration(Config.class);

    @Test
    void bindsExistingCanonicalDefaultsWithoutDatabase() {
        runner.run(context -> {
            assertNull(context.getStartupFailure());
            assertEquals(AssessmentBlockCalculatorTests.properties(), context.getBean(AssessmentProperties.class));
            assertFalse(context.containsBean("dataSource"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"algorithm-version=other", "minimum-questions=19", "maximum-questions=51",
            "block-size=5", "promote-correct-threshold=2", "demote-correct-threshold=2",
            "stable-hold-blocks-required=1", "starting-level=A2", "options-per-question=3",
            "minimum-vocabulary-per-level=29"})
    void rejectsConfigurationThatChangesLockedV1Contract(String override) {
        runner.withPropertyValues("app.assessment." + override)
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AssessmentProperties.class)
    static class Config {
    }
}
