package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SemanticResolutionResult {

    private final List<ExpressionDiagnostic> diagnostics;
    private final SemanticModel model;

    private SemanticResolutionResult(List<ExpressionDiagnostic> diagnostics, SemanticModel model) {
        Objects.requireNonNull(diagnostics, "diagnostics");
        this.diagnostics = List.copyOf(diagnostics);
        this.model = model;
        if (hasErrors(this.diagnostics) && model != null) {
            throw new IllegalArgumentException("model must be absent when semantic errors are present");
        }
        if (!hasErrors(this.diagnostics) && model == null) {
            throw new IllegalArgumentException("model must be present when semantic errors are absent");
        }
        if (model != null && !this.diagnostics.equals(model.diagnostics())) {
            throw new IllegalArgumentException("result diagnostics must match semantic model diagnostics");
        }
    }

    public static SemanticResolutionResult withoutModel(List<ExpressionDiagnostic> diagnostics) {
        return new SemanticResolutionResult(diagnostics, null);
    }

    public static SemanticResolutionResult withModel(SemanticModel model) {
        Objects.requireNonNull(model, "model");
        return new SemanticResolutionResult(model.diagnostics(), model);
    }

    public List<ExpressionDiagnostic> diagnostics() {
        return diagnostics;
    }

    public Optional<SemanticModel> model() {
        return Optional.ofNullable(model);
    }

    public boolean hasErrors() {
        return hasErrors(diagnostics);
    }

    private static boolean hasErrors(List<ExpressionDiagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR);
    }
}
