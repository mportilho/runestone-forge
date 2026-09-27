package com.runestone.expeval_mk3.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

final class Stage12StressSupport {

    private static final long CHILD_TIMEOUT_SECONDS = 90;

    private Stage12StressSupport() {
    }

    static void runInConstrainedTemurinChild(Class<?> mainClass, Path output) throws Exception {
        Files.createDirectories(output.getParent());
        Process child = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xms512m", "-Xmx512m", "-Xss1m", "-cp", System.getProperty("java.class.path"),
                mainClass.getName())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
            if (!child.waitFor(CHILD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionError("stress child timeout; see " + output);
            }
            if (child.exitValue() != 0) {
                throw new AssertionError("stress child exited " + child.exitValue() + "; see " + output);
            }
        } finally {
            awaitForcedTermination(child, output);
        }
    }

    static void requireTemurin21() {
        if (!"21".equals(System.getProperty("java.specification.version"))
                || !System.getProperty("java.vendor", "").contains("Eclipse Adoptium")) {
            throw new AssertionError("Stage 12 stress requires Temurin 21, found "
                    + System.getProperty("java.vendor") + " " + System.getProperty("java.runtime.version"));
        }
    }

    private static void awaitForcedTermination(Process child, Path output) throws InterruptedException {
        if (!child.isAlive()) {
            return;
        }
        child.destroyForcibly();
        if (!child.waitFor(10, TimeUnit.SECONDS)) {
            throw new AssertionError("stress child did not terminate after forceful shutdown; see " + output);
        }
    }
}
