package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;

final class AstNodeLimitException extends RuntimeException {

    private final SourceSpan span;

    AstNodeLimitException(SourceSpan span) {
        super("AST node count exceeds the compilation limit", null, false, false);
        this.span = span;
    }

    ExpressionDiagnostic diagnostic() {
        return ExpressionDiagnostics.create(DiagnosticCode.COMPILE_AST_NODE_COUNT_EXCEEDED, getMessage(), span);
    }
}
