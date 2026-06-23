package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.UnaryOperator;

import java.util.Objects;

record ExecutableUnaryOp(UnaryOperator operator, ExecutableNode operand) implements ExecutableNode {

    ExecutableUnaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(operand, "operand must not be null");
    }
}
