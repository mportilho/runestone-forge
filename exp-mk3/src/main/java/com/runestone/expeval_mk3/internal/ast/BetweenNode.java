package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import java.util.Objects;

record BetweenNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode value,
        SourceSpan operatorSpan,
        boolean negated,
        ExpressionNode lowerBound,
        ExpressionNode upperBound) implements ExpressionNode {

    BetweenNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(operatorSpan, "operatorSpan");
        Objects.requireNonNull(lowerBound, "lowerBound");
        Objects.requireNonNull(upperBound, "upperBound");
    }
}
