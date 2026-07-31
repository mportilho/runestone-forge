package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;
import java.util.Objects;

public record LiteralNode(NodeId id, SourceSpan sourceSpan, LiteralValue value) implements ExpressionNode {

    public LiteralNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(value, "value");
    }
}
