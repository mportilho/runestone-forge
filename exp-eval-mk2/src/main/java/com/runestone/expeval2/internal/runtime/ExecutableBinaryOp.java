package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.BinaryOperator;

import java.util.Objects;

record ExecutableBinaryOp(BinaryOperator operator, ExecutableNode left, ExecutableNode right) implements ExecutableNode {

    ExecutableBinaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
    }
}
