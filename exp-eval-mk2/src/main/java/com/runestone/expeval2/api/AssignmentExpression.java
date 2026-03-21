package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;

import java.util.Map;
import java.util.Objects;

public final class AssignmentExpression {

    private final ExpressionRuntimeSupport runtime;

    private AssignmentExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    public static AssignmentExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    public static AssignmentExpression compile(String source, ExpressionEnvironment environment) {
        return new AssignmentExpression(ExpressionRuntimeSupport.compileAssignments(source, environment));
    }

    public AssignmentExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public Map<String, Object> compute() {
        return runtime.computeAssignments();
    }

    public AuditResult<Map<String, Object>> computeWithAudit() {
        return runtime.computeAssignmentsWithAudit();
    }
}
