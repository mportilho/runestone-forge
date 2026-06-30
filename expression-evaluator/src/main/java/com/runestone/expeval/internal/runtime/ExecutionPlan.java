package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;

record ExecutionPlan(
        List<ExecutableAssignment> assignments,
        ExecutableNode resultExpression,
        Object[] defaults,
        Map<String, ExternalBindingPlan> externalBindings,
        int externalSymbolsCount,
        int maxAuditEvents,
        List<AuditEvent> foldedVariableReads,
        boolean containsDynamicInstant) {

    ExecutionPlan {
        assignments = List.copyOf(Objects.requireNonNull(assignments, "assignments must not be null"));
        foldedVariableReads = List.copyOf(Objects.requireNonNull(foldedVariableReads, "foldedVariableReads must not be null"));
        // resultExpression is null for assignment-only mode (AssignmentExpression)
    }
}
