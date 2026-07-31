package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.DiagnosticSeverity;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;

import java.util.List;
import java.util.Objects;

public record SemanticResolutionSuccess(
        SemanticModel model,
        List<ExpressionDiagnostic> warnings) implements SemanticResolutionResult {

    public SemanticResolutionSuccess {
        Objects.requireNonNull(model, "model");
        warnings = List.copyOf(warnings);
        if (warnings.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
            throw new IllegalArgumentException("successful semantic resolution must not carry error diagnostics");
        }
    }
}
