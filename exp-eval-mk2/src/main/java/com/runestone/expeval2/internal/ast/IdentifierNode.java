package com.runestone.expeval2.internal.ast;

public record IdentifierNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    String name
) implements ExpressionNode {

    public IdentifierNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonBlank(name, "name");
    }
}
