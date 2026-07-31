package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionCompilationExceptionTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 3, 1, 1);

    @Test
    void rejectsAnEmptyDiagnosticList() {
        assertThatThrownBy(() -> new ExpressionCompilationException(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exposesImmutablePublicDiagnosticsDirectly() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "SEMANTIC_UNKNOWN_SYMBOL", "Unknown symbol: x", SPAN);
        List<ExpressionDiagnostic> source = new ArrayList<>(List.of(diagnostic));

        ExpressionCompilationException exception = new ExpressionCompilationException(source);
        source.add(ExpressionDiagnostic.error(DiagnosticCategory.SEMANTIC, "OTHER", "other", SPAN));

        assertThat(exception.diagnostics()).containsExactly(diagnostic);
        assertThatThrownBy(() -> exception.diagnostics().add(diagnostic))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void messageIncludesTheFirstDiagnosticMessage() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "SEMANTIC_UNKNOWN_SYMBOL", "Unknown symbol: x", SPAN);

        ExpressionCompilationException exception = new ExpressionCompilationException(List.of(diagnostic));

        assertThat(exception.getMessage()).contains("Unknown symbol: x");
    }
}
