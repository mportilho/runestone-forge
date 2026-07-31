package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Reports the first Phase 5 runtime execution failure crossing the public boundary. Carries exactly
 * one immutable {@link ExpressionDiagnostic}; execution stops on the first failure because assignments
 * and impure functions make continuing unsafe. Source-originated failures carry that node's
 * {@link SourceSpan}; purely external failures, such as an undeclared override key, may omit one.
 */
public final class ExpressionExecutionException extends RuntimeException {

    private final ExpressionDiagnostic diagnostic;

    private ExpressionExecutionException(ExpressionDiagnostic diagnostic, Throwable cause) {
        super(message(diagnostic), cause);
        this.diagnostic = diagnostic;
    }

    public static ExpressionExecutionException of(ExpressionDiagnostic diagnostic) {
        return new ExpressionExecutionException(Objects.requireNonNull(diagnostic, "diagnostic"), null);
    }

    public static ExpressionExecutionException of(ExpressionDiagnostic diagnostic, Throwable cause) {
        Objects.requireNonNull(diagnostic, "diagnostic");
        Objects.requireNonNull(cause, "cause");
        return new ExpressionExecutionException(diagnostic, cause);
    }

    public ExpressionDiagnostic diagnostic() {
        return diagnostic;
    }

    private static String message(ExpressionDiagnostic diagnostic) {
        Objects.requireNonNull(diagnostic, "diagnostic");
        return "Expression execution failed: " + diagnostic.message();
    }
}
