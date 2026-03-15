package com.runestone.expeval2.ast;

public record LiteralNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    String value
) implements ExpressionNode {

    public LiteralNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonNull(value, "value");
    }
}
