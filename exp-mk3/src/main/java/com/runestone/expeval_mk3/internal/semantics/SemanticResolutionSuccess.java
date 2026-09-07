package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.DiagnosticSeverity;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostics;

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
