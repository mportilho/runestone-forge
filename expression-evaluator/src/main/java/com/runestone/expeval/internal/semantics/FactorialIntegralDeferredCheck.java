package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

public record FactorialIntegralDeferredCheck(NodeId nodeId, SourceSpan sourceSpan) implements DeferredCheck {

    public FactorialIntegralDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_FACTORIAL_NOT_INTEGRAL;
    }
}
