package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

public record CurrentTemporalValueNode(NodeId id, SourceSpan sourceSpan, CurrentTemporalValueKind kind) implements ExpressionNode {

    public CurrentTemporalValueNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(kind, "kind");
    }
}
