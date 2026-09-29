package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

public record FactorialMaxBoundDeferredCheck(NodeId nodeId, SourceSpan sourceSpan, int maxFactorialInput)
        implements DeferredCheck {

    public FactorialMaxBoundDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (maxFactorialInput < 0) {
            throw new IllegalArgumentException("maxFactorialInput must not be negative");
        }
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_FACTORIAL_EXCEEDS_MAXIMUM;
    }
}
