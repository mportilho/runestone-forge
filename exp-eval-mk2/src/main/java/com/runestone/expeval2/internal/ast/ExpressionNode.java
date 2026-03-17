package com.runestone.expeval2.internal.ast;

public sealed interface ExpressionNode extends Node permits
    BinaryOperationNode,
    ConditionalNode,
    FunctionCallNode,
    IdentifierNode,
    LiteralNode,
    PostfixOperationNode,
    UnaryOperationNode,
    VectorLiteralNode {
}
