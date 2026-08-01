package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CompiledExpression {

    private final ExecutionPlan plan;
    private final RuntimeServices runtimeServices;
    private final List<ExpressionDiagnostic> compilationDiagnostics;

    CompiledExpression(ExecutionPlan plan, RuntimeServices runtimeServices, List<ExpressionDiagnostic> compilationDiagnostics) {
        this.plan = Objects.requireNonNull(plan, "plan");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.compilationDiagnostics = List.copyOf(Objects.requireNonNull(compilationDiagnostics, "compilationDiagnostics"));
    }

    public Object compute() {
        return compute(Map.of());
    }

    public Object compute(Map<String, ?> overrides) {
        ExpressionViewSupport.requireResultType(plan);
        return plan.compute(overrides, runtimeServices.clock());
    }

    public List<ExpressionDiagnostic> compilationDiagnostics() {
        return compilationDiagnostics;
    }

    public ResultExpression asResult() {
        return new ResultExpression(plan, runtimeServices);
    }

    public MathExpression asMath() {
        return new MathExpression(plan, runtimeServices);
    }

    public LogicalExpression asLogical() {
        return new LogicalExpression(plan, runtimeServices);
    }

    public AssignmentsExpression asAssignments() {
        return new AssignmentsExpression(plan, runtimeServices);
    }
}
