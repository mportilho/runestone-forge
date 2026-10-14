package com.runestone.expeval.internal.ast;

public sealed interface ExpressionNode extends AstNode permits BetweenNode, BinaryOperationNode, ConditionalNode,
        CollectionLiteralNode, CurrentItemNode, CurrentTemporalValueNode, FunctionCallNode, GroupedExpressionNode, IdentifierNode,
        LiteralNode, MembershipNode, NavigationChainNode, NullCoalesceNode, PostfixOperationNode, UnaryOperationNode {
}
