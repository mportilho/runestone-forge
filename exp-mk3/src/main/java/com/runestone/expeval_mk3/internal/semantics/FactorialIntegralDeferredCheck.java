package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

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
