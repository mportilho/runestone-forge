package com.runestone.expeval.internal.ast;

public sealed interface Node permits AssignmentNode, ExpressionFileNode, ExpressionNode {

    NodeId nodeId();

    SourceSpan sourceSpan();
}
