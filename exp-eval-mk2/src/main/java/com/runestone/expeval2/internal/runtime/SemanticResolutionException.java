package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.SourceSpan;

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
        Objects.requireNonNull(issues, "issues must not be null");
        if (issues.isEmpty()) {
            return "semantic resolution failed for expression:\n\n  " + source;
        }
        SemanticIssue first = issues.getFirst();
        String[] lines = source.split("\n", -1);
        SourceSpan span = first.sourceSpan();
        int lineIdx = span.startLine() - 1;
        String sourceLine = (lineIdx >= 0 && lineIdx < lines.length) ? lines[lineIdx] : source;
        int caretLen = Math.max(1, span.endColumn() - span.startColumn());
        caretLen = Math.min(caretLen, Math.max(1, sourceLine.length() - span.startColumn()));
        String pointer = " ".repeat(Math.max(0, span.startColumn())) + "^".repeat(caretLen);
        return "semantic resolution failed:\n\n  %s\n  %s\n  %s at %d:%d \u2014 %s".formatted(
            sourceLine, pointer, first.code(), span.startLine(), span.startColumn(), first.message()
        );
    }
}
