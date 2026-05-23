package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.ast.UnaryOperator;

import java.util.Objects;

public record ExecutableUnaryOp(UnaryOperator operator, ExecutableNode operand) implements ExecutableNode {

    public ExecutableUnaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(operand, "operand must not be null");
    }
}
