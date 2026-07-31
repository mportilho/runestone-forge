package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

/**
 * A statically-known index or slice bound against a receiver whose size is not known at compile time.
 */
public record SubscriptBoundsDeferredCheck(NodeId nodeId, SourceSpan sourceSpan) implements DeferredCheck {

    public SubscriptBoundsDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS;
    }
}
