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
        return "semantic resolution failed:\n\n"
                + SourcePointerFormatter.format(source, pos, first.code(), first.message());
    }
}
