package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.util.Map;
import java.util.Objects;

public final class CompiledExpression {

    private final ExecutionPlan plan;
    private final RuntimeServices runtimeServices;

    CompiledExpression(ExecutionPlan plan) {
        this(plan, RuntimeServices.systemDefault());
    }

    CompiledExpression(ExecutionPlan plan, RuntimeServices runtimeServices) {
        this.plan = Objects.requireNonNull(plan, "plan");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
    }

    public Object compute() {
        return compute(Map.of());
    }

    public Object compute(Map<String, ?> overrides) {
        return plan.compute(overrides, runtimeServices.clock());
    }
}
