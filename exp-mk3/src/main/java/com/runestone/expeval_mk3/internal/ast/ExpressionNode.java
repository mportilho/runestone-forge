package com.runestone.expeval_mk3.internal.ast;

sealed interface ExpressionNode extends AstNode permits BetweenNode, BinaryOperationNode, ConditionalNode,
        CollectionLiteralNode, CurrentItemNode, CurrentTemporalValueNode, FunctionCallNode, GroupedExpressionNode, IdentifierNode,
        LiteralNode, MembershipNode, NavigationChainNode, NullCoalesceNode, PostfixOperationNode, UnaryOperationNode {
}
