package com.runestone.expeval2.semantic;

import com.runestone.expeval2.ast.SourceSpan;

import java.util.Objects;

public record SemanticIssue(
    String code,
    SemanticIssueSeverity severity,
    String message,
    SourceSpan sourceSpan
) {

    public SemanticIssue {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        Objects.requireNonNull(severity, "severity must not be null");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
    }
}
