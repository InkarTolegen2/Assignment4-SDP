package claims;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ArchitectureTest {
    private static final List<String> SUBSYSTEMS = List.of("intake", "policy", "payout");

    @Test
    void subsystemsKnowNothingAboutFacade() throws IOException {
        for (String subsystem : SUBSYSTEMS) {
            assertNoImport(subsystem, "claims.facade");
        }
    }

    @Test
    void subsystemsDoNotKnowEachOther() throws IOException {
        for (String subsystem : SUBSYSTEMS) {
            for (String other : SUBSYSTEMS) {
                if (!subsystem.equals(other)) {
                    assertNoImport(subsystem, "claims." + other);
                }
            }
        }
    }

    private void assertNoImport(String subsystem, String forbidden) throws IOException {
        Path dir = Paths.get("src/main/java/claims", subsystem);
        List<Path> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream.filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList());
        }
        for (Path file : files) {
            String code = Files.readString(file);
            assertFalse(code.contains("import " + forbidden), file + " imports " + forbidden);
        }
    }
}