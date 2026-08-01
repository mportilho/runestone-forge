package com.runestone.expeval_mk3.corpus;

import java.util.Objects;

/**
 * A runtime-phase invalid case whose failure is not yet routed through the structured diagnostic seam
 * (e.g. Phase 6 navigation/wildcard accessor failures that still leak a raw Java exception type). Prefer
 * {@link ExpectedRuntimeDiagnostic} for any failure that crosses the boundary as an
 * {@code ExpressionExecutionException}.
 */
record ExpectedRuntimeError(String type, String messageContains) implements ExpectedOutcome {

    ExpectedRuntimeError {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(messageContains, "messageContains");
    }
}
