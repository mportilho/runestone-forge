package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record ConcreteTypeRestriction(NodeId nodeId, ExpressionType requiredType, SourceSpan sourceSpan) {

    public ConcreteTypeRestriction {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(requiredType, "requiredType");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
