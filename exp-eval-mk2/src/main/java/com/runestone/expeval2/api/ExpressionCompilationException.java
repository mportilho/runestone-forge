package com.runestone.expeval2.api;

import java.util.List;
import java.util.Objects;

public final class ExpressionCompilationException extends RuntimeException {

    private final String source;
    private final List<CompilationIssue> issues;

    public ExpressionCompilationException(String source, List<CompilationIssue> issues, Throwable cause) {
        super(buildMessage(source, issues), cause);
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues must not be null"));
    }

    public String source() {
        return source;
    }

    public List<CompilationIssue> issues() {
        return issues;
    }

    private static String buildMessage(String source, List<CompilationIssue> issues) {
        if (issues.isEmpty()) {
            return "compilation failed for expression:\n\n  " + source;
        }
        CompilationIssue first = issues.getFirst();
        if (first.position() != null) {
            return "compilation failed:\n\n" + formatWithPointer(source, first.position(), first.code(), first.message());
        }
        String detail = issues.stream()
            .map(issue -> issue.code() + ": " + issue.message())
            .findFirst()
            .orElse("compilation failed");
        return "compilation failed for expression '" + source + "': " + detail;
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
