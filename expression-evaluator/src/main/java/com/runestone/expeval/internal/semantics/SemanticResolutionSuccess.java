package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.DiagnosticSeverity;
import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;

import java.util.List;
import java.util.Objects;

public record SemanticResolutionSuccess(
        SemanticModel model,
        List<ExpressionDiagnostic> warnings) implements SemanticResolutionResult {

    public SemanticResolutionSuccess {
        Objects.requireNonNull(model, "model");
        warnings = ExpressionDiagnostics.canonicalCopy(warnings);
        if (warnings.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
            throw new IllegalArgumentException("successful semantic resolution must not carry error diagnostics");
        }
    }
}
