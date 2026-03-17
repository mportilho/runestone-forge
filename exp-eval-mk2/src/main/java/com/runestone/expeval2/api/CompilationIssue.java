package com.runestone.expeval2.api;

public record CompilationIssue(String code, String message) {

    public CompilationIssue {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
