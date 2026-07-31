package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record BinaryOperationNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode left,
        BinaryOperator operator,
        SourceSpan operatorSpan,
        ExpressionNode right) implements ExpressionNode {

    public BinaryOperationNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(operatorSpan, "operatorSpan");
        Objects.requireNonNull(right, "right");
    }
}
