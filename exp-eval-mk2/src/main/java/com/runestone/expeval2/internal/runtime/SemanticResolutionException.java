package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Objects;

public final class SemanticResolutionException extends RuntimeException {

    private final String source;
    private final List<SemanticIssue> issues;

    public SemanticResolutionException(String source, List<SemanticIssue> issues) {
        super(buildMessage(source, issues));
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues must not be null"));
    }

    public String source() {
        return source;
    }

    public List<SemanticIssue> issues() {
        return issues;
    }

    private static String buildMessage(String source, List<SemanticIssue> issues) {
        Objects.requireNonNull(source, "source must not be null");
        List<SemanticIssue> safeIssues = List.copyOf(Objects.requireNonNull(issues, "issues must not be null"));
        String detail = safeIssues.stream()
            .map(issue -> issue.code() + ": " + issue.message())
            .findFirst()
            .orElse("semantic resolution failed");
        return "Semantic resolution failed for source '" + source + "': " + detail;
    }
}
