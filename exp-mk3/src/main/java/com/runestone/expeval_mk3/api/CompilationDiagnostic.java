package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.Objects;

public record CompilationDiagnostic(
        String category,
        String code,
        String message,
        int offset,
        int endOffset,
        int line,
        int column) {

    public CompilationDiagnostic {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        if (offset < 0 || endOffset < offset || line < 1 || column < 1) {
            throw new IllegalArgumentException("invalid diagnostic source span");
        }
    }

    static CompilationDiagnostic from(ExpressionDiagnostic diagnostic) {
        return new CompilationDiagnostic(
                diagnostic.category().name(),
                diagnostic.code().name(),
                diagnostic.message(),
                diagnostic.span().offset(),
                diagnostic.span().endOffset(),
                diagnostic.span().line(),
                diagnostic.span().column());
    }
}
