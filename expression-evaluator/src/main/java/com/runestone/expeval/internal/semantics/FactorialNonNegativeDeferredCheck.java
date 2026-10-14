package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

public record FactorialNonNegativeDeferredCheck(NodeId nodeId, SourceSpan sourceSpan) implements DeferredCheck {

    public FactorialNonNegativeDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_FACTORIAL_NEGATIVE;
    }
}
