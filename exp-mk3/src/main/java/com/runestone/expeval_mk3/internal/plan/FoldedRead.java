package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/**
 * Records that a symbol read collapsed into a compile-time constant (ADR 0019, issue #117): the only
 * case in Etapa 7 is a non-overridable External Symbol, whose effective value is its validated
 * environment default. This is construction-time metadata the Etapa 10 audit will consume to explain
 * a value that no longer appears as a read at execution; it adds no branch to the hot path.
 */
record FoldedRead(String symbolName, NodeId nodeId, SourceSpan sourceSpan, Object foldedValue) {

    FoldedRead {
        Objects.requireNonNull(symbolName, "symbolName");
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(foldedValue, "foldedValue");
        if (symbolName.isBlank()) {
            throw new IllegalArgumentException("symbolName must not be blank");
        }
    }
}
