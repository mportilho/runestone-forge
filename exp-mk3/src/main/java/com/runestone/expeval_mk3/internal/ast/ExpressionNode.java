package com.runestone.expeval_mk3.internal.ast;

sealed interface ExpressionNode extends AstNode permits BetweenNode, BinaryOperationNode, CurrentTemporalValueNode,
        GroupedExpressionNode, IdentifierNode, LiteralNode, MembershipNode, NullCoalescenceNode, PostfixOperationNode,
        UnaryOperationNode {
}
