package com.runestone.expeval2.ast;

public record BinaryOperationNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    BinaryOperator operator,
    ExpressionNode left,
    ExpressionNode right
) implements ExpressionNode {

    public BinaryOperationNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonNull(operator, "operator");
        AstValidation.requireNonNull(left, "left");
        AstValidation.requireNonNull(right, "right");
    }
}
