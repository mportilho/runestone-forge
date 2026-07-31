package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

public record BetweenExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        boolean negated,
        ExecutableNode value,
        ExecutableNode lowerBound,
        ExecutableNode upperBound,
        ExpressionType operandType) implements ExecutableNode {

    public BetweenExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(lowerBound, "lowerBound");
        Objects.requireNonNull(upperBound, "upperBound");
        Objects.requireNonNull(operandType, "operandType");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object evaluatedValue = value.execute(scope);
        if (ExpressionRuntime.compareValues(evaluatedValue, lowerBound.execute(scope), operandType) < 0) {
            return negated;
        }
        boolean inside = ExpressionRuntime.compareValues(evaluatedValue, upperBound.execute(scope), operandType) <= 0;
        return inside != negated;
    }
}
