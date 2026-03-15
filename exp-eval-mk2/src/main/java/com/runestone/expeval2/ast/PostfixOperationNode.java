package com.runestone.expeval2.ast;

public record PostfixOperationNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    PostfixOperator operator,
    ExpressionNode operand
) implements ExpressionNode {

    public PostfixOperationNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonNull(operator, "operator");
        AstValidation.requireNonNull(operand, "operand");
    }
}
