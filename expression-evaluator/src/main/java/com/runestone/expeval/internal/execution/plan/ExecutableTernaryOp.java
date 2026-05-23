package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.ast.TernaryOperator;

import java.util.Objects;

public record ExecutableTernaryOp(
        TernaryOperator operator,
        ExecutableNode first,
        ExecutableNode second,
        ExecutableNode third
) implements ExecutableNode {

    public ExecutableTernaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(first,    "first must not be null");
        Objects.requireNonNull(second,   "second must not be null");
        Objects.requireNonNull(third,    "third must not be null");
    }
}
