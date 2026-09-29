package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

public sealed interface AstNode permits AssignmentNode, AssignmentTargetNode, ConditionalBranchNode, ExpressionFileNode,
        ExpressionNode, LambdaNode, NavigationLink {

    NodeId id();

    SourceSpan sourceSpan();
}
