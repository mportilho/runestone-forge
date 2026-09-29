package com.runestone.expeval.internal.parser;

import com.runestone.expeval.api.ExpressionDiagnostic;

/** Terminal phase abort; never exposed through the public compilation API. */
final class CompilationLimitException extends RuntimeException {

    private final ExpressionDiagnostic diagnostic;

    CompilationLimitException(ExpressionDiagnostic diagnostic) {
        super(diagnostic.message(), null, false, false);
        this.diagnostic = diagnostic;
    }

    ExpressionDiagnostic diagnostic() {
        return diagnostic;
    }
}
