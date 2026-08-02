package com.runestone.expeval_mk3.internal.diagnostics;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.SourceSpan;

import java.math.BigInteger;

/**
 * The single seam scalar/numeric-domain execution, function-provider invocation, and navigation
 * execution use to raise a positioned {@link ExpressionExecutionException} instead of leaking raw
 * {@code ArithmeticException}, {@code ClassCastException}, {@code IndexOutOfBoundsException},
 * {@code NullPointerException}, or big-math implementation details across the public boundary.
 *
 * <p>Per ADR 0018 there is deliberately no factory for a null navigation receiver: the semantic
 * resolver rejects a nullable receiver on a strict link, so a null receiver reaching the runtime is an
 * internal invariant violation rather than a public diagnostic.
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

    public static ExpressionExecutionException subscriptOutOfBounds(BigInteger index, int size, SourceSpan span) {
        String message = "collection index is outside the collection bounds: " + index + " over size " + size;
        return ExpressionExecutionException.of(diagnostic(DiagnosticCode.RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS, message, span));
    }

    public static ExpressionExecutionException mapKeyNotFound(String key, SourceSpan span) {
        return ExpressionExecutionException.of(
                diagnostic(DiagnosticCode.RUNTIME_MAP_KEY_NOT_FOUND, "map key not found: " + key, span));
    }

    public static ExpressionExecutionException memberAccessFailure(String memberName, SourceSpan span, Throwable cause) {
        String message = "member access failed: " + memberName;
        return ExpressionExecutionException.of(
                diagnostic(DiagnosticCode.RUNTIME_MEMBER_ACCESS_FAILURE, message, span), cause);
    }

    public static ExpressionExecutionException invalidOperationArgument(String message, SourceSpan span) {
        return ExpressionExecutionException.of(
                diagnostic(DiagnosticCode.RUNTIME_INVALID_OPERATION_ARGUMENT, message, span));
    }

    public static ExpressionExecutionException materializationLimitExceeded(
            int size, int maxMaterializedSize, SourceSpan span) {
        String message = "materialized collection size " + size + " exceeds maxMaterializedSize " + maxMaterializedSize;
        return ExpressionExecutionException.of(
                diagnostic(DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED, message, span));
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
