package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.ast.PostfixOperator;

import java.util.Objects;

public record ExecutablePostfixOp(PostfixOperator operator, ExecutableNode operand) implements ExecutableNode {

    public ExecutablePostfixOp {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(operand, "operand must not be null");
    }
}
