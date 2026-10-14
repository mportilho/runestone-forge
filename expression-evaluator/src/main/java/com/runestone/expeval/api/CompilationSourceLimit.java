package com.runestone.expeval.api;

import com.runestone.expeval.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;

import java.util.List;

final class CompilationSourceLimit {

    private CompilationSourceLimit() {
    }

    static boolean exceeded(String source, ExpressionEnvironment environment) {
        return environment.trustMode() != ExpressionTrustMode.UNSAFE
                && source.length() > environment.resourceLimits().maxSourceLength();
    }

    static ExpressionCompilationResult.Failure failure() {
        // Do not scan, copy or retain an oversized source to construct its diagnostic.
        return new ExpressionCompilationResult.Failure(List.of(ExpressionDiagnostics.create(
                DiagnosticCode.COMPILE_SOURCE_LENGTH_EXCEEDED,
                "Expression source length exceeds the compilation limit",
                new SourceSpan(0, 0, 1, 1))));
    }
}
