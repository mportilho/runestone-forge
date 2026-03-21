package com.runestone.expeval2.api;

public record CompilationIssue(String code, String message, CompilationPosition position) {

    public CompilationIssue {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        // position may be null — no source location available
    }

    public CompilationIssue(String code, String message) {
        this(code, message, null);
    }
}
