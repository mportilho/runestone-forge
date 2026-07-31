package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

public record DestructuringMinimumSizeDeferredCheck(NodeId nodeId, SourceSpan sourceSpan, int minimumSize)
        implements DeferredCheck {

    public DestructuringMinimumSizeDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (minimumSize < 0) {
            throw new IllegalArgumentException("minimumSize must not be negative");
        }
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_DESTRUCTURING_SIZE_TOO_SMALL;
    }
}
