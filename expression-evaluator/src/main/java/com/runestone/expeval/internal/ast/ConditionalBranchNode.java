package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

public record ConditionalBranchNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode condition,
        ExpressionNode consequence) implements AstNode {

    public ConditionalBranchNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(consequence, "consequence");
    }
}
