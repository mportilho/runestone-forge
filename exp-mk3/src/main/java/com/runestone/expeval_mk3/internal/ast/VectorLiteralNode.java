package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import java.util.List;
import java.util.Objects;

record VectorLiteralNode(NodeId id, SourceSpan sourceSpan, List<ExpressionNode> elements) implements ExpressionNode {

    VectorLiteralNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(elements, "elements");
        elements = List.copyOf(elements);
    }
}
