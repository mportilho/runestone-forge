package com.runestone.expeval_mk3.api;

import java.util.List;

public final class ExpressionCompilationException extends IllegalArgumentException {

    private final List<CompilationDiagnostic> diagnostics;

    ExpressionCompilationException(List<com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic> diagnostics) {
        super(message(diagnostics));
        this.diagnostics = diagnostics.stream().map(CompilationDiagnostic::from).toList();
        if (this.diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
    }

    public List<CompilationDiagnostic> diagnostics() {
        return diagnostics;
    }

    private static String message(List<com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return "Expression compilation failed";
        }
        return "Expression compilation failed: " + diagnostics.getFirst().message();
    }
}
