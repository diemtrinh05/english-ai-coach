package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.englishaicoach.config.SrsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SrsPropertiesValidationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=test")
            .withUserConfiguration(SrsCalculatorTests.PropertiesConfiguration.class);

    @Test
    void canonicalDefaultsBindWithoutDatabase() {
        runner.run(context -> {
            assertNull(context.getStartupFailure());
            assertNotNull(context.getBean(SrsProperties.class));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"algorithm-version=other", "minimum-ease-factor=0",
            "initial-ease-factor=1", "minimum-interval-days=0", "maximum-interval-days=0",
            "first-success-interval-days=0", "second-success-interval-days=181",
            "response-time-reference=0ms", "response-time-floor=0.5ms",
            "minimum-time-factor=1.2", "maximum-time-factor=0", "correct-quality-threshold=4",
            "mastered-minimum-quality=6", "mastered-minimum-repetitions=0",
            "mastered-minimum-interval-days=181"})
    void rejectsBrokenTypedConfigurationAtBinding(String property) {
        runner.withPropertyValues("app.srs." + property)
                .run(context -> assertNotNull(context.getStartupFailure()));
    }
}
