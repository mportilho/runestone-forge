package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;

import java.util.Objects;

/**
 * A value-dependent power domain precondition per ADR 0017: the base may be negative with a
 * dynamically-determined reduced exponent denominator parity, or the base may be zero with a
 * dynamically-determined exponent sign.
 */
public record PowerRealDomainDeferredCheck(NodeId nodeId, SourceSpan sourceSpan) implements DeferredCheck {

    public PowerRealDomainDeferredCheck {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public DiagnosticCode runtimeCode() {
        return DiagnosticCode.RUNTIME_POWER_COMPLEX_DOMAIN;
    }
}
