package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

public sealed interface AstNode permits AssignmentNode, AssignmentTargetNode, ConditionalBranchNode, ExpressionFileNode,
        ExpressionNode, LambdaNode, NavigationLink {

    NodeId id();

    SourceSpan sourceSpan();
}
