package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

public record CurrentItemNode(NodeId id, SourceSpan sourceSpan) implements ExpressionNode {

    public CurrentItemNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
