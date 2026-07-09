package com.runestone.expeval_mk3.internal.ast;

sealed interface ExpressionNode extends AstNode permits CurrentTemporalValueNode, IdentifierNode, LiteralNode {
}
