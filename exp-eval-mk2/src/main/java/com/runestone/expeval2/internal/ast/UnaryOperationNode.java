package com.runestone.expeval2.internal.ast;

public record UnaryOperationNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    UnaryOperator operator,
    ExpressionNode operand
) implements ExpressionNode {

    public UnaryOperationNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonNull(operator, "operator");
        AstValidation.requireNonNull(operand, "operand");
    }
}
