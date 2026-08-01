package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * A thin, immutable view over a compiled plan's final result, accepting only a {@link ScalarType#NUMBER}
 * result.
 */
public final class MathExpression {

    private final ExecutionPlan plan;
    private final RuntimeServices runtimeServices;
    private final SourceSpan resultSourceSpan;

    MathExpression(ExecutionPlan plan, RuntimeServices runtimeServices) {
        this.plan = plan;
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        ExpressionType resultType = ExpressionViewSupport.requireResultType(plan);
        this.resultSourceSpan = plan.resultSourceSpan();
        ExpressionViewSupport.requirePubliclyExposable(resultType, resultSourceSpan);
        ExpressionViewSupport.requireExactScalarType(resultType, ScalarType.NUMBER, resultSourceSpan);
    }

    public BigDecimal compute() {
        return compute(Map.of());
    }

    public BigDecimal compute(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        Object value = plan.compute(overrides, runtimeServices.clock());
        return (BigDecimal) PublicMaterialization.materialize(
                value, ScalarType.NUMBER, plan.maxMaterializedSize(), resultSourceSpan);
    }
}
