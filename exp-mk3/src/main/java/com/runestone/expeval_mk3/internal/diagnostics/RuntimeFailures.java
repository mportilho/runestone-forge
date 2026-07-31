package com.runestone.expeval_mk3.internal.diagnostics;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.SourceSpan;

/**
 * The single seam scalar/numeric-domain execution and function-provider invocation use to raise a
 * positioned {@link ExpressionExecutionException} instead of leaking raw {@code ArithmeticException},
 * {@code ClassCastException}, {@code NullPointerException}, or big-math implementation details across
 * the public boundary. Navigation/filter-specific runtime failures are Phase 6 work and are not routed
 * through this seam.
 */
public final class RuntimeFailures {

    private RuntimeFailures() {
    }

    public static ExpressionExecutionException invalidExternalInput(String message) {
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_INVALID_EXTERNAL_INPUT, message, null));
    }

    public static ExpressionExecutionException forbiddenNull(String message, SourceSpan span) {
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_FORBIDDEN_NULL, message, span));
    }

    public static ExpressionExecutionException undefinedOperation(String message, SourceSpan span) {
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_UNDEFINED_OPERATION, message, span));
    }

    public static ExpressionExecutionException calculationFailure(String message, SourceSpan span, Throwable cause) {
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_CALCULATION_FAILURE, message, span), cause);
    }

    public static ExpressionExecutionException domainViolation(DiagnosticCode code, String message, SourceSpan span) {
        return ExpressionExecutionException.of(diagnostic(code, message, span));
    }

    public static ExpressionExecutionException domainViolation(
            DiagnosticCode code, String message, SourceSpan span, Throwable cause) {
        return ExpressionExecutionException.of(diagnostic(code, message, span), cause);
    }

    public static ExpressionExecutionException destructuringInsufficient(int required, int actual, SourceSpan span) {
        String message = "destructuring source does not contain enough elements: expected at least "
                + required + " but found " + actual;
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_DESTRUCTURING_SIZE_TOO_SMALL, message, span));
    }

    public static ExpressionExecutionException providerFailure(String functionName, SourceSpan span, Throwable cause) {
        String message = "function provider failed: " + functionName;
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_PROVIDER_FAILURE, message, span), cause);
    }

    public static ExpressionExecutionException providerReturnViolation(
            String functionName, SourceSpan span, ProviderReturnViolation violation) {
        String message = "function return contract violated: " + functionName + ": " + violation.getMessage();
        return ExpressionExecutionException.of(diagnostic(violation.code(), message, span), violation);
    }

    private static ExpressionDiagnostic diagnostic(DiagnosticCode code, String message, SourceSpan span) {
        return ExpressionDiagnostic.error(DiagnosticCategory.RUNTIME, code.name(), message, span);
    }
}
