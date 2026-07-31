package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record CurrentItemNode(NodeId id, SourceSpan sourceSpan) implements ExpressionNode {

    public CurrentItemNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
