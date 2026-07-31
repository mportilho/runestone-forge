package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

public record MaterializationLimitDeferredCheck(NodeId nodeId, SourceSpan sourceSpan, int maxMaterializedSize)
        implements DeferredCheck {

    public MaterializationLimitDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (maxMaterializedSize < 0) {
            throw new IllegalArgumentException("maxMaterializedSize must not be negative");
        }
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED;
    }
}
