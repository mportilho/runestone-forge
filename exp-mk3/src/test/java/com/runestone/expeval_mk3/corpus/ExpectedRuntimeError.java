package com.runestone.expeval_mk3.corpus;

import java.util.Objects;

/**
 * A runtime-phase invalid case whose failure is not routed through the structured diagnostic seam and
 * still crosses the boundary as a raw Java exception type. Navigation failures no longer belong here:
 * ADR 0018 routes every one of them through {@code RuntimeFailures}. Prefer
 * {@link ExpectedRuntimeDiagnostic} for any failure that crosses the boundary as an
 * {@code ExpressionExecutionException}.
 */
record ExpectedRuntimeError(String type, String messageContains) implements ExpectedOutcome {

    ExpectedRuntimeError {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(messageContains, "messageContains");
    }
}
