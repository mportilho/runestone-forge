package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.AssignedSymbol;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A thin, immutable view over a compiled plan's internal-symbol assignments. Executes only the source
 * assignments, in order, and deliberately skips an optional final result expression: it is never
 * evaluated, produces no effects, and is never part of the returned map.
 */
public final class AssignmentsExpression {

    private final ExecutionPlan plan;
    private final RuntimeServices runtimeServices;
    private final List<AssignedSymbol> assignedSymbols;

    AssignmentsExpression(ExecutionPlan plan, RuntimeServices runtimeServices) {
        this.plan = plan;
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.assignedSymbols = plan.assignedSymbolsInCreationOrder();
        ExpressionViewSupport.requireAtLeastOneAssignment(assignedSymbols);
        ExpressionViewSupport.requireAllAssignedSymbolsPubliclyExposable(assignedSymbols);
        ExpressionViewSupport.requireWithinAssignmentMaterializationLimit(assignedSymbols, plan.maxMaterializedSize());
    }

    public Map<String, Object> compute() {
        return compute(Map.of());
    }

    public Map<String, Object> compute(Map<String, ?> overrides) {
        return plan.computeMaterializedAssignments(overrides, runtimeServices.clock());
    }

    public ComputationWithMemory<Map<String, Object>> computeWithMemory() {
        return computeWithMemory(Map.of());
    }

    public ComputationWithMemory<Map<String, Object>> computeWithMemory(Map<String, ?> overrides) {
        return plan.computeAssignmentsWithMemory(overrides, runtimeServices.clock());
    }
}
