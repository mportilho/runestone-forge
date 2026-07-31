package com.runestone.expeval_mk3.internal.parser;

import com.runestone.expeval_mk3.api.ExpressionDiagnostic;

import java.util.List;
import java.util.Objects;

public record ParseFailure(List<ExpressionDiagnostic> diagnostics, PredictionPath predictionPath) implements ParseResult {

    public ParseFailure {
        Objects.requireNonNull(diagnostics, "diagnostics");
        if (diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
        diagnostics = List.copyOf(diagnostics);
        Objects.requireNonNull(predictionPath, "predictionPath");
    }
}
