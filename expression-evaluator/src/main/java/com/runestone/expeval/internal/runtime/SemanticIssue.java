package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.SourceSpan;

import java.util.Objects;

public record SemanticIssue(
    IssueCode code,
    SemanticIssueSeverity severity,
    String message,
    SourceSpan sourceSpan
) {

    public SemanticIssue {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
    }
}
