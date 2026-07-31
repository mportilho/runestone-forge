package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.UnaryOperator;

import java.util.Objects;

public record UnaryExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        UnaryOperator operator,
        ExecutableNode operand) implements ExecutableNode {

    public UnaryExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(operand, "operand");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return switch (operator) {
            case NEGATE -> ExpressionRuntime.number(operand.execute(scope)).negate();
            case LOGICAL_NOT -> !ExpressionRuntime.bool(operand.execute(scope));
        };
    }
}
