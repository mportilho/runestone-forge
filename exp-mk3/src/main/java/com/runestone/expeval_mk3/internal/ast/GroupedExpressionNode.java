package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import java.util.Objects;

record GroupedExpressionNode(NodeId id, SourceSpan sourceSpan, ExpressionNode expression) implements ExpressionNode {

    GroupedExpressionNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(expression, "expression");
    }
}
