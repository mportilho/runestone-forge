package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionExecutionExceptionTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 3, 1, 1);

    @Test
    void exposesTheSingleDiagnostic() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "RUNTIME_PROVIDER_FAILURE", "function provider failed: sample", SPAN);

        ExpressionExecutionException exception = ExpressionExecutionException.of(diagnostic);

        assertThat(exception.diagnostic()).isEqualTo(diagnostic);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void preservesTheOriginalCause() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "RUNTIME_PROVIDER_FAILURE", "function provider failed: sample", SPAN);
        IllegalStateException cause = new IllegalStateException("boom");

        ExpressionExecutionException exception = ExpressionExecutionException.of(diagnostic, cause);

        assertThat(exception.diagnostic()).isEqualTo(diagnostic);
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void allowsAnAbsentPrimarySpanForPurelyExternalFailures() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "RUNTIME_INVALID_EXTERNAL_INPUT", "unknown external symbol override: z", null);

        ExpressionExecutionException exception = ExpressionExecutionException.of(diagnostic);

        assertThat(exception.diagnostic().primarySpan()).isEmpty();
    }

    @Test
    void messageIncludesTheDiagnosticMessage() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "RUNTIME_PROVIDER_FAILURE", "function provider failed: sample", SPAN);

        ExpressionExecutionException exception = ExpressionExecutionException.of(diagnostic);

        assertThat(exception.getMessage()).contains("function provider failed: sample");
    }

    @Test
    void requiresACause() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "RUNTIME_PROVIDER_FAILURE", "function provider failed: sample", SPAN);

        assertThatThrownBy(() -> ExpressionExecutionException.of(diagnostic, null))
                .isInstanceOf(NullPointerException.class);
    }
}
