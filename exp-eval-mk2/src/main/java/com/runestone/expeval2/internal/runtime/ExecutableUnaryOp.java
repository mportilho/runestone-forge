package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.UnaryOperator;

import java.util.Objects;

record ExecutableUnaryOp(UnaryOperator operator, ExecutableNode operand) implements ExecutableNode {

    ExecutableUnaryOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(operand, "operand must not be null");
    }
}
