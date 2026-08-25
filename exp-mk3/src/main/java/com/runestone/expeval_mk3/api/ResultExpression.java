package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.PublicMaterialization;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.util.Map;
import java.util.Objects;

/**
 * A thin, immutable view over a compiled plan's final result, accepting any result type that is
 * publicly exposable.
 */
public final class ResultExpression {

    private final ExecutionPlan plan;
    private final RuntimeServices runtimeServices;
    private final ExpressionType resultType;
    private final SourceSpan resultSourceSpan;

    ResultExpression(ExecutionPlan plan, RuntimeServices runtimeServices) {
        this.plan = plan;
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.resultType = ExpressionViewSupport.requireResultType(plan);
        this.resultSourceSpan = plan.resultSourceSpan();
        ExpressionViewSupport.requirePubliclyExposable(resultType, resultSourceSpan);
    }

    public Object compute() {
        return compute(Map.of());
    }

    public Object compute(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        Object value = plan.compute(overrides, runtimeServices.clock());
        return PublicMaterialization.materialize(value, resultType, plan.maxMaterializedSize(), resultSourceSpan);
    }

    public ComputationWithMemory<Object> computeWithMemory() {
        return computeWithMemory(Map.of());
    }

    public ComputationWithMemory<Object> computeWithMemory(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        return plan.computeWithMemory(overrides, runtimeServices.clock());
    }
}
