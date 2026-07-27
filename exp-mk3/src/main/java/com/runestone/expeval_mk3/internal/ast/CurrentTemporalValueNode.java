package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record CurrentTemporalValueNode(NodeId id, SourceSpan sourceSpan, CurrentTemporalValueKind kind) implements ExpressionNode {

    public CurrentTemporalValueNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(kind, "kind");
    }
}
