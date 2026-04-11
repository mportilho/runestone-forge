package com.runestone.expeval.internal.ast;

public record TernaryOperationNode(
        NodeId nodeId,
        SourceSpan sourceSpan,
        TernaryOperator operator,
        ExpressionNode first,
        ExpressionNode second,
        ExpressionNode third
) implements ExpressionNode {
}
