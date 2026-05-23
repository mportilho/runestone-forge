package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.ast.BinaryOperator;

import java.util.Objects;

public record ExecutableBinaryOp(BinaryOperator operator, ExecutableNode left, ExecutableNode right) implements ExecutableNode {

    public ExecutableBinaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
    }
}
