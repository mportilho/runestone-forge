package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record GroupedExpressionNode(NodeId id, SourceSpan sourceSpan, ExpressionNode expression) implements ExpressionNode {

    public GroupedExpressionNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(expression, "expression");
    }
}
