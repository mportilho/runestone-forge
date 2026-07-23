package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record AssignmentNode(
        NodeId id,
        SourceSpan sourceSpan,
        AssignmentTargetNode target,
        ExpressionNode expression) implements AstNode {

    public AssignmentNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(expression, "expression");
    }
}
