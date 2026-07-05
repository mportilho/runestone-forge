package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

sealed interface AstNode permits AssignmentNode, AssignmentTargetNode, ExpressionFileNode, ExpressionNode {

    NodeId id();

    SourceSpan sourceSpan();
}
