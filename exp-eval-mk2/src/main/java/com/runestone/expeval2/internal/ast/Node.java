package com.runestone.expeval2.internal.ast;

public sealed interface Node permits AssignmentNode, ExpressionFileNode, ExpressionNode {

    NodeId nodeId();

    SourceSpan sourceSpan();
}
