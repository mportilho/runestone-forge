package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.DiagnosticSeverity;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;

import java.util.List;

public record SemanticResolutionFailure(List<ExpressionDiagnostic> diagnostics) implements SemanticResolutionResult {

    public SemanticResolutionFailure {
        diagnostics = List.copyOf(diagnostics);
        if (diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
        if (diagnostics.stream().noneMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
            throw new IllegalArgumentException("failed semantic resolution must contain at least one error diagnostic");
        }
    }
}
