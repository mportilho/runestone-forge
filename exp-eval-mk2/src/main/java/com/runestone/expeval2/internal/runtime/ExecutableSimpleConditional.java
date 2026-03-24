package com.runestone.expeval2.internal.runtime;

import java.util.Objects;

record ExecutableSimpleConditional(
        ExecutableNode condition,
        ExecutableNode thenExpression,
        ExecutableNode elseExpression
) implements ExecutableNode {

    ExecutableSimpleConditional {
        Objects.requireNonNull(condition, "condition must not be null");
        Objects.requireNonNull(thenExpression, "thenExpression must not be null");
        Objects.requireNonNull(elseExpression, "elseExpression must not be null");
    }
}
