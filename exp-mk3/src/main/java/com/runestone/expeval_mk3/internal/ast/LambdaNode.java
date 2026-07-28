package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record LambdaNode(
        NodeId id,
        SourceSpan sourceSpan,
        CurrentItemNode currentItem,
        SourceSpan arrowSpan,
        ExpressionNode body) implements AstNode {

    public LambdaNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(currentItem, "currentItem");
        Objects.requireNonNull(arrowSpan, "arrowSpan");
        Objects.requireNonNull(body, "body");
    }
}
