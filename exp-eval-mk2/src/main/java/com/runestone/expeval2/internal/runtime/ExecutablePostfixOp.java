package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.PostfixOperator;

import java.util.Objects;

record ExecutablePostfixOp(PostfixOperator operator, ExecutableNode operand) implements ExecutableNode {

    ExecutablePostfixOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(operand, "operand must not be null");
    }
}
