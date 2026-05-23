package com.runestone.expeval.internal.execution.plan;

import java.util.Objects;

public record ExecutableSimpleConditional(
        ExecutableNode condition,
        ExecutableNode thenExpression,
        ExecutableNode elseExpression
) implements ExecutableNode {

    public ExecutableSimpleConditional {
        Objects.requireNonNull(condition, "condition must not be null");
        Objects.requireNonNull(thenExpression, "thenExpression must not be null");
        Objects.requireNonNull(elseExpression, "elseExpression must not be null");
    }
}
