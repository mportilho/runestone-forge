package com.runestone.expeval.api;

import java.util.Objects;

final class SourcePointerFormatter {

    private SourcePointerFormatter() {
    }

    static String format(String source, CompilationPosition position, Object code, String message) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");

        String[] lines = source.split("\n", -1);
        int lineIndex = position.line() - 1;
        String sourceLine = lineIndex >= 0 && lineIndex < lines.length ? lines[lineIndex] : source;
        int caretLength = Math.max(1, position.endColumn() - position.column());
        caretLength = Math.min(caretLength, Math.max(1, sourceLine.length() - position.column()));
        String pointer = " ".repeat(Math.max(0, position.column())) + "^".repeat(caretLength);
        return "  %s\n  %s\n  %s at %d:%d \u2014 %s".formatted(
                sourceLine,
                pointer,
                code,
                position.line(),
                position.column(),
                message);
    }
}
