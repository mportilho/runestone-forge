package com.runestone.expeval.api;

import java.util.Objects;

public final class ExpressionEvaluationException extends RuntimeException {

    private final String source;

    public ExpressionEvaluationException(String source, String code, String message, CompilationPosition position) {
        super(buildMessage(source, code, message, position));
        this.source = Objects.requireNonNull(source, "source must not be null");
    }

    public String source() {
        return source;
    }

    private static String buildMessage(String source, String code, String message, CompilationPosition position) {
        if (position == null) {
            return "evaluation failed for expression '" + source + "': " + code + ": " + message;
        }
        return "evaluation failed:\n\n" + formatWithPointer(source, position, code, message);
    }

    private static String formatWithPointer(String source, CompilationPosition pos, String code, String message) {
        String[] lines = source.split("\n", -1);
        int lineIdx = pos.line() - 1;
        String sourceLine = (lineIdx >= 0 && lineIdx < lines.length) ? lines[lineIdx] : source;
        int caretLen = Math.max(1, pos.endColumn() - pos.column());
        caretLen = Math.min(caretLen, Math.max(1, sourceLine.length() - pos.column()));
        String pointer = " ".repeat(Math.max(0, pos.column())) + "^".repeat(caretLen);
        return "  %s\n  %s\n  %s at %d:%d \u2014 %s".formatted(sourceLine, pointer, code, pos.line(), pos.column(), message);
    }
}
