package com.runestone.expeval_mk3.support;

import java.nio.file.Files;
import java.nio.file.Path;

/** Locates versioned MK3 documentation from module and reactor test launches. */
public final class DocumentationPaths {

    private DocumentationPaths() {
    }

    public static Path moduleRoot() {
        Path current = Path.of(System.getProperty("user.dir"));
        return Files.isDirectory(current.resolve("docs/reference")) ? current : current.resolve("exp-mk3");
    }
}
