package com.runestone.expeval.api;

import java.util.Objects;

public record CompilationIssue(IssueCode code, String message, CompilationPosition position) {

    public CompilationIssue {
        Objects.requireNonNull(code, "code must not be null");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        // position may be null — no source location available
    }

    public CompilationIssue(IssueCode code, String message) {
        this(code, message, null);
    }
}
