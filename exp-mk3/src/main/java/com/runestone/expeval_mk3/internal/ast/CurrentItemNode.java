package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record CurrentItemNode(NodeId id, SourceSpan sourceSpan) implements ExpressionNode {

    CurrentItemNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
