package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;

import java.util.Map;
import java.util.Objects;

public final class CompiledExpression {

    private final ExecutionPlan plan;

    CompiledExpression(ExecutionPlan plan) {
        this.plan = Objects.requireNonNull(plan, "plan");
    }

    public Object compute() {
        return compute(Map.of());
    }

    public Object compute(Map<String, ?> overrides) {
        return plan.compute(overrides);
    }
}
