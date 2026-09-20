package com.example.englishaicoach;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public final class ThemeResourceInvariantTest {

    private static final Path MAIN_RESOURCES = Path.of("src", "main", "res");

    @Test
    public void v1ThemeIsExplicitlyLightWithoutNightOverride() throws IOException {
        String theme = readSource(MAIN_RESOURCES.resolve("values/themes.xml"));

        assertTrue(theme.contains("Theme.Material3.Light.NoActionBar"));
        assertFalse(theme.contains("Theme.Material3.DayNight"));
        assertFalse(Files.exists(MAIN_RESOURCES.resolve("values-night/themes.xml")));
    }

    @Test
    public void runtimeDoesNotEnableNightMode() throws IOException {
        Path sourceRoot = Path.of("src", "main", "java");
        try (var sources = Files.walk(sourceRoot)) {
            boolean enablesNightMode = sources
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(ThemeResourceInvariantTest::readSource)
                    .anyMatch(source -> source.contains("AppCompatDelegate")
                            || source.contains("MODE_NIGHT"));

            assertFalse(enablesNightMode);
        }
    }

    private static String readSource(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể đọc Android source để kiểm tra theme.", exception);
        }
    }
}
