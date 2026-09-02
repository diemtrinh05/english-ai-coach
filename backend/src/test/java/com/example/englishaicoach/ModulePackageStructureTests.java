package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModulePackageStructureTests {

    private static final List<String> REQUIRED_MODULES = List.of(
            "auth",
            "user",
            "onboarding",
            "vocabulary",
            "learning",
            "personalization",
            "quiz",
            "progress",
            "gamification",
            "notification",
            "ai",
            "admin",
            "audit",
            "common");

    @Test
    void requiredModulePackagesExist() throws IOException {
        Path packageRoot = locatePackageRoot();

        for (String module : REQUIRED_MODULES) {
            Path packageInfo = packageRoot.resolve(module).resolve("package-info.java");
            assertTrue(Files.isRegularFile(packageInfo), () -> "Thiếu package skeleton: " + module);

            String source = Files.readString(packageInfo);
            String expectedDeclaration = "package com.example.englishaicoach." + module + ";";
            assertTrue(source.contains(expectedDeclaration), () -> "Sai package declaration: " + module);
        }
    }

    @Test
    void moduleSkeletonsIntroduceNoDependencies() throws IOException {
        Path packageRoot = locatePackageRoot();

        for (String module : REQUIRED_MODULES) {
            Path packageInfo = packageRoot.resolve(module).resolve("package-info.java");
            boolean hasImport = Files.readAllLines(packageInfo).stream()
                    .map(String::strip)
                    .anyMatch(line -> line.startsWith("import "));

            assertFalse(hasImport, () -> "Package skeleton tạo dependency ngoài ý muốn: " + module);
        }
    }

    private Path locatePackageRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath();
        Path backendRoot = workingDirectory.resolve("src/main/java/com/example/englishaicoach");
        if (Files.isDirectory(backendRoot)) {
            return backendRoot;
        }

        return workingDirectory.resolve("backend/src/main/java/com/example/englishaicoach");
    }
}
