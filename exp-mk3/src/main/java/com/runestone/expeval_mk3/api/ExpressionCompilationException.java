package com.runestone.expeval_mk3.api;

import java.util.List;

public final class ExpressionCompilationException extends IllegalArgumentException {

    private final List<ExpressionDiagnostic> diagnostics;

    ExpressionCompilationException(List<ExpressionDiagnostic> diagnostics) {
        super(message(diagnostics));
        this.diagnostics = List.copyOf(diagnostics);
        if (this.diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
    }

    public List<ExpressionDiagnostic> diagnostics() {
        return diagnostics;
    }

    private static String message(List<ExpressionDiagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return "Expression compilation failed";
        }
        return "Expression compilation failed: " + diagnostics.getFirst().message();
    }
}
