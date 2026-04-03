package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Map;
import java.util.Objects;

record ExecutionPlan(
        List<ExecutableAssignment> assignments,
        ExecutableNode resultExpression,
        Object[] defaults,
        Map<String, ExternalBindingPlan> externalBindings,
        int externalSymbolsCount,
        int maxAuditEvents) {

    ExecutionPlan {
        assignments = List.copyOf(Objects.requireNonNull(assignments, "assignments must not be null"));
        // resultExpression is null for assignment-only mode (AssignmentExpression)
    }
}
