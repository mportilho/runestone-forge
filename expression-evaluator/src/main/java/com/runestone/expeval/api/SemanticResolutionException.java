package com.runestone.expeval.api;

import java.util.List;
import java.util.Objects;

public final class SemanticResolutionException extends RuntimeException {

    private final String source;
    private final List<CompilationIssue> issues;

    public SemanticResolutionException(String source, List<CompilationIssue> issues) {
        super(buildMessage(source, issues));
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
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(issues, "issues must not be null");
        if (issues.isEmpty()) {
            return "semantic resolution failed for expression:\n\n  " + source;
        }
        CompilationIssue first = issues.getFirst();
        CompilationPosition pos = first.position();
        if (pos == null) {
            return "semantic resolution failed for expression '" + source + "': " + first.code() + ": " + first.message();
        }
        String[] lines = source.split("\n", -1);
        int lineIdx = pos.line() - 1;
        String sourceLine = (lineIdx >= 0 && lineIdx < lines.length) ? lines[lineIdx] : source;
        int caretLen = Math.max(1, pos.endColumn() - pos.column());
        caretLen = Math.min(caretLen, Math.max(1, sourceLine.length() - pos.column()));
        String pointer = " ".repeat(Math.max(0, pos.column())) + "^".repeat(caretLen);
        return "semantic resolution failed:\n\n  %s\n  %s\n  %s at %d:%d \u2014 %s".formatted(
                sourceLine, pointer, first.code(), pos.line(), pos.column(), first.message()
        );
    }
}
