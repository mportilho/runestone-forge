package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

/**
 * A value-dependent root domain precondition per ADR 0017: the radicand may be negative with a
 * dynamically-determined reduced degree numerator parity, the degree may dynamically be zero, or the
 * radicand may be zero with a dynamically-determined degree sign.
 */
public record RootRealDomainDeferredCheck(NodeId nodeId, SourceSpan sourceSpan) implements DeferredCheck {

    public RootRealDomainDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_ROOT_DOMAIN_VIOLATION;
    }
}
