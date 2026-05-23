package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.api.AuditEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ExecutionPlan(
        List<ExecutableAssignment> assignments,
        ExecutableNode resultExpression,
        Object[] defaults,
        Map<String, ExternalBindingPlan> externalBindings,
        int externalSymbolsCount,
        int maxAuditEvents,
        List<AuditEvent> foldedVariableReads) {

    public ExecutionPlan {
        assignments = List.copyOf(Objects.requireNonNull(assignments, "assignments must not be null"));
        foldedVariableReads = List.copyOf(Objects.requireNonNull(foldedVariableReads, "foldedVariableReads must not be null"));
        // resultExpression is null for assignment-only mode (AssignmentExpression)
    }
}
