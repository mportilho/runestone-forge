package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record UnaryOperationNode(
        NodeId id,
        SourceSpan sourceSpan,
        UnaryOperator operator,
        SourceSpan operatorSpan,
        ExpressionNode operand) implements ExpressionNode {

    public UnaryOperationNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(operatorSpan, "operatorSpan");
        Objects.requireNonNull(operand, "operand");
    }
}
