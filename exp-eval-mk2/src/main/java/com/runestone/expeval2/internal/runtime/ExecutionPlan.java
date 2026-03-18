package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutionPlan(List<ExecutableAssignment> assignments, ExecutableNode resultExpression) {

    ExecutionPlan {
        assignments = List.copyOf(Objects.requireNonNull(assignments, "assignments must not be null"));
        Objects.requireNonNull(resultExpression, "resultExpression must not be null");
    }
}
