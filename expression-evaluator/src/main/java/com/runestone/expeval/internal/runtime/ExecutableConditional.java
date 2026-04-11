package com.runestone.expeval.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutableConditional(
        List<ExecutableNode> conditions,
        List<ExecutableNode> results,
        ExecutableNode elseExpression
) implements ExecutableNode {

    ExecutableConditional {
        conditions = List.copyOf(Objects.requireNonNull(conditions, "conditions must not be null"));
        results = List.copyOf(Objects.requireNonNull(results, "results must not be null"));
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        if (conditions.size() != results.size()) {
            throw new IllegalArgumentException("conditions and results must have the same size");
        }
        Objects.requireNonNull(elseExpression, "elseExpression must not be null");
    }
}
