package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.AssignedSymbol;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.util.Collections;
import java.util.LinkedHashMap;
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
        Objects.requireNonNull(overrides, "overrides");
        List<Object> rawValues = plan.computeAssignedValues(overrides, runtimeServices.clock());
        // LinkedHashMap preserves first-creation order; Map.copyOf/immutable factories do not.
        Map<String, Object> materialized = new LinkedHashMap<>();
        for (int index = 0; index < assignedSymbols.size(); index++) {
            AssignedSymbol symbol = assignedSymbols.get(index);
            materialized.put(symbol.name(), PublicMaterialization.materialize(
                    rawValues.get(index), symbol.type(), plan.maxMaterializedSize(), symbol.sourceSpan()));
        }
        return Collections.unmodifiableMap(materialized);
    }
}
