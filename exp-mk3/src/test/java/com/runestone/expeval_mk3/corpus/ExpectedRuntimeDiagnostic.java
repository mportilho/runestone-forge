package com.runestone.expeval_mk3.corpus;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.List;
import java.util.Objects;

/**
 * A runtime-phase invalid case whose failure crosses the public boundary as an
 * {@code ExpressionExecutionException} carrying a stable {@code ExpressionDiagnostic}. Unlike
 * {@link ExpectedRuntimeError}, which pins a raw Java exception type for failures not yet routed through
 * that seam (Phase 6 navigation work), this asserts the structured category/code/span contract.
 */
record ExpectedRuntimeDiagnostic(String code, List<SourceSpan> spans) implements ExpectedOutcome {

    ExpectedRuntimeDiagnostic {
        Objects.requireNonNull(code, "code");
        spans = List.copyOf(Objects.requireNonNull(spans, "spans"));
    }

    SourceSpan requiredSpan() {
        if (spans.isEmpty()) {
            throw new IllegalStateException("runtime diagnostic does not declare a source span");
        }
        return spans.getFirst();
    }
}
