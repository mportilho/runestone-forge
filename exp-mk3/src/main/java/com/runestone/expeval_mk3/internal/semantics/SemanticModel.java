package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.internal.ast.ExpressionSemanticTree;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.List;
import java.util.Objects;

public record SemanticModel(
        ExpressionSemanticTree sourceTree,
        ExpressionEnvironment environment,
        List<ExpressionDiagnostic> diagnostics) {

    public SemanticModel {
        Objects.requireNonNull(sourceTree, "sourceTree");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(diagnostics, "diagnostics");
        diagnostics = List.copyOf(diagnostics);
        if (diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
            throw new IllegalArgumentException("semantic model cannot contain error diagnostics");
        }
    }
}
