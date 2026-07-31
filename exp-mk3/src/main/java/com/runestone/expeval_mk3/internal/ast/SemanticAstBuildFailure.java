package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionDiagnostic;

import java.util.List;
import java.util.Objects;

public record SemanticAstBuildFailure(List<ExpressionDiagnostic> diagnostics) implements SemanticAstBuildResult {

    public SemanticAstBuildFailure {
        Objects.requireNonNull(diagnostics, "diagnostics");
        if (diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
        diagnostics = List.copyOf(diagnostics);
    }
}
