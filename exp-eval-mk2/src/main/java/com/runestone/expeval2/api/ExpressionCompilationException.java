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
        String detail = issues.stream()
            .map(issue -> issue.code() + ": " + issue.message())
            .findFirst()
            .orElse("compilation failed");
        return "Compilation failed for expression '" + source + "': " + detail;
    }
}
