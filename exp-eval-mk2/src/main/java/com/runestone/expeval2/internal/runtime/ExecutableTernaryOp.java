package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.TernaryOperator;

import java.util.Objects;

record ExecutableTernaryOp(
        TernaryOperator operator,
        ExecutableNode first,
        ExecutableNode second,
        ExecutableNode third
) implements ExecutableNode {

    ExecutableTernaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(first,    "first must not be null");
        Objects.requireNonNull(second,   "second must not be null");
        Objects.requireNonNull(third,    "third must not be null");
    }
}
