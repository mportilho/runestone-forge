package com.runestone.expeval_mk3.internal.diagnostics;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record ExpressionDiagnostic(
        DiagnosticCategory category,
        DiagnosticCode code,
        String message,
        SourceSpan span) {

    public ExpressionDiagnostic {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(span, "span");
    }
}
