package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.PublicMaterialization;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.util.Map;
import java.util.Objects;

/**
 * A thin, immutable view over a compiled plan's final result, accepting only a
 * {@link ScalarType#BOOLEAN} result.
 */
public final class LogicalExpression {

    private final ExecutionPlan plan;
    private final RuntimeServices runtimeServices;
    private final SourceSpan resultSourceSpan;

    LogicalExpression(ExecutionPlan plan, RuntimeServices runtimeServices) {
        this.plan = plan;
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        ExpressionType resultType = ExpressionViewSupport.requireResultType(plan);
        this.resultSourceSpan = plan.resultSourceSpan();
        ExpressionViewSupport.requirePubliclyExposable(resultType, resultSourceSpan);
        ExpressionViewSupport.requireExactScalarType(resultType, ScalarType.BOOLEAN, resultSourceSpan);
    }

    public boolean compute() {
        return compute(Map.of());
    }

    public boolean compute(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        Object value = plan.compute(overrides, runtimeServices.clock());
        return (Boolean) PublicMaterialization.materialize(
                value, ScalarType.BOOLEAN, plan.maxMaterializedSize(), resultSourceSpan);
    }

    public ComputationWithMemory<Boolean> computeWithMemory() {
        return computeWithMemory(Map.of());
    }

    public ComputationWithMemory<Boolean> computeWithMemory(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        return ExpressionViewSupport.narrow(plan.computeWithMemory(overrides, runtimeServices.clock()));
    }
}
