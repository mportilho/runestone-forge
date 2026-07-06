package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

sealed interface AstNode permits AssignmentNode, AssignmentTargetNode, CollectionOperationArgument,
        ConditionalBranchNode, ExpressionFileNode, ExpressionNode, LambdaNode, NavigationLink {

    NodeId id();

    SourceSpan sourceSpan();
}
