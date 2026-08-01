package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;

public sealed interface ExpressionCompilationResult {

    record Success(CompiledExpression compiledExpression, List<ExpressionDiagnostic> diagnostics)
            implements ExpressionCompilationResult {

        public Success {
            Objects.requireNonNull(compiledExpression, "compiledExpression");
            Objects.requireNonNull(diagnostics, "diagnostics");
            diagnostics = List.copyOf(diagnostics);
            if (diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
                throw new IllegalArgumentException("successful compilation must not carry error diagnostics");
            }
        }
    }

    record Failure(List<ExpressionDiagnostic> diagnostics) implements ExpressionCompilationResult {

        public Failure {
            Objects.requireNonNull(diagnostics, "diagnostics");
            diagnostics = List.copyOf(diagnostics);
            if (diagnostics.isEmpty()) {
                throw new IllegalArgumentException("diagnostics must not be empty");
            }
            if (diagnostics.stream().noneMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
                throw new IllegalArgumentException("failed compilation must contain at least one error diagnostic");
            }
        }
    }
}
