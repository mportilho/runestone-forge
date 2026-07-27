package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record BetweenNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode value,
        boolean negated,
        SourceSpan operatorSpan,
        ExpressionNode lowerBound,
        SourceSpan lowerSeparatorSpan,
        ExpressionNode upperBound) implements ExpressionNode {

    public BetweenNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(operatorSpan, "operatorSpan");
        Objects.requireNonNull(lowerBound, "lowerBound");
        Objects.requireNonNull(lowerSeparatorSpan, "lowerSeparatorSpan");
        Objects.requireNonNull(upperBound, "upperBound");
    }
}
