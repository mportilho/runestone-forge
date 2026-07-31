package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.List;
import java.util.Objects;

public record CollectionLiteralNode(NodeId id, SourceSpan sourceSpan, List<ExpressionNode> elements) implements ExpressionNode {

    public CollectionLiteralNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        elements = List.copyOf(elements);
    }
}
