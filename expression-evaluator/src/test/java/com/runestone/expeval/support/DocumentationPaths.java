package com.runestone.expeval.support;

import java.nio.file.Files;
import java.nio.file.Path;

/** Locates versioned expression-evaluator documentation from module and reactor test launches. */
public final class DocumentationPaths {

    private DocumentationPaths() {
    }

    public static Path moduleRoot() {
        Path current = Path.of(System.getProperty("user.dir"));
        return Files.isDirectory(current.resolve("docs/reference")) ? current : current.resolve("expression-evaluator");
    }
}
