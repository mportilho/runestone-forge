package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostics;

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
