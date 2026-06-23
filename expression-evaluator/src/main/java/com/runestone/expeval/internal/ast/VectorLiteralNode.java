package com.runestone.expeval.internal.ast;

import java.util.List;

public record VectorLiteralNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    List<ExpressionNode> elements
) implements ExpressionNode {

    public VectorLiteralNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        elements = AstValidation.copyList(elements, "elements");
    }
}
