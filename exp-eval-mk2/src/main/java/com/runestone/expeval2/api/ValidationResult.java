package com.runestone.expeval2.api;

import java.util.List;
import java.util.Objects;

public record ValidationResult(String source, boolean valid, List<CompilationIssue> issues) {

    public ValidationResult {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(issues, "issues must not be null");
        issues = List.copyOf(issues);
    }

    public static ValidationResult ok(String source) {
        return new ValidationResult(source, true, List.of());
    }

    public static ValidationResult failed(String source, List<CompilationIssue> issues) {
        return new ValidationResult(source, false, issues);
    }

    public String formatMessage() {
        if (valid) {
            return "expression is valid: " + source;
        }
        if (issues.isEmpty()) {
            return "validation failed for expression:\n\n  " + source;
        }
        CompilationIssue first = issues.getFirst();
        if (first.position() != null) {
            return "validation failed:\n\n" + formatWithPointer(first.position(), first.code(), first.message());
        }
        String detail = issues.stream()
            .map(issue -> issue.code() + ": " + issue.message())
            .findFirst()
            .orElse("validation failed");
        return "validation failed for expression '" + source + "': " + detail;
    }

    private String formatWithPointer(CompilationPosition pos, String code, String message) {
        String[] lines = source.split("\n", -1);
        int lineIdx = pos.line() - 1;
        String sourceLine = (lineIdx >= 0 && lineIdx < lines.length) ? lines[lineIdx] : source;
        int caretLen = Math.max(1, pos.endColumn() - pos.column());
        caretLen = Math.min(caretLen, Math.max(1, sourceLine.length() - pos.column()));
        String pointer = " ".repeat(Math.max(0, pos.column())) + "^".repeat(caretLen);
        return "  %s\n  %s\n  %s at %d:%d \u2014 %s".formatted(sourceLine, pointer, code, pos.line(), pos.column(), message);
    }
}
