package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.DiagnosticSeverity;
import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;

import java.util.List;

public record SemanticResolutionFailure(List<ExpressionDiagnostic> diagnostics) implements SemanticResolutionResult {

    public SemanticResolutionFailure {
        diagnostics = ExpressionDiagnostics.canonicalCopy(diagnostics);
        if (diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
        if (diagnostics.stream().noneMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
            throw new IllegalArgumentException("failed semantic resolution must contain at least one error diagnostic");
        }
    }
}
