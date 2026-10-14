package com.runestone.expeval.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class Stage12TestSuiteContractTest {

    private static final List<String> FORBIDDEN_SKIP_MARKERS = List.of(
            "@" + "Disabled", "Assumptions." + "abort", "Assumptions." + "assume", "Test" + "AbortedException");

    @Test
    void testSourcesDoNotDisableOrAbortCoverage() throws IOException {
        List<Path> sourceFiles;
        try (Stream<Path> paths = Files.walk(Path.of("src", "test", "java"))) {
            sourceFiles = paths.filter(path -> path.toString().endsWith(".java")).toList();
        }

        List<String> violations = new ArrayList<>();
        for (Path sourceFile : sourceFiles) {
            String source = Files.readString(sourceFile);
            for (String marker : FORBIDDEN_SKIP_MARKERS) {
                if (source.contains(marker)) {
                    violations.add(sourceFile + ": " + marker);
                }
            }
        }

        assertThat(violations).isEmpty();
    }
}
