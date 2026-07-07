package com.runestone.expeval_mk3.internal.diagnostics;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public final class ExpressionDiagnostic {

    private final DiagnosticCategory category;
    private final DiagnosticCode code;
    private final DiagnosticSeverity severity;
    private final String message;
    private final SourceSpan span;

    public ExpressionDiagnostic(
            DiagnosticCategory category,
            DiagnosticCode code,
            String message,
            SourceSpan span) {
        this(DiagnosticSeverity.ERROR, new DiagnosticContent(category, code, message, span));
    }

    private ExpressionDiagnostic(DiagnosticSeverity severity, DiagnosticContent content) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.category = content.category();
        this.code = content.code();
        this.message = content.message();
        this.span = content.span();
    }

    public static ExpressionDiagnostic error(
            DiagnosticCategory category,
            DiagnosticCode code,
            String message,
            SourceSpan span) {
        return new ExpressionDiagnostic(category, code, message, span);
    }

    public static ExpressionDiagnostic warning(
            DiagnosticCategory category,
            DiagnosticCode code,
            String message,
            SourceSpan span) {
        return new ExpressionDiagnostic(
                DiagnosticSeverity.WARNING,
                new DiagnosticContent(category, code, message, span));
    }

    public DiagnosticCategory category() {
        return category;
    }

    public DiagnosticCode code() {
        return code;
    }

    public DiagnosticSeverity severity() {
        return severity;
    }

    public String message() {
        return message;
    }

    public SourceSpan span() {
        return span;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExpressionDiagnostic that)) {
            return false;
        }
        return category == that.category
                && code == that.code
                && severity == that.severity
                && message.equals(that.message)
                && span.equals(that.span);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, code, severity, message, span);
    }

    @Override
    public String toString() {
        return "ExpressionDiagnostic[category=" + category
                + ", code=" + code
                + ", severity=" + severity
                + ", message=" + message
                + ", span=" + span
                + ']';
    }

    private record DiagnosticContent(
            DiagnosticCategory category,
            DiagnosticCode code,
            String message,
            SourceSpan span) {

        private DiagnosticContent {
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(message, "message");
            Objects.requireNonNull(span, "span");
        }
    }
}
