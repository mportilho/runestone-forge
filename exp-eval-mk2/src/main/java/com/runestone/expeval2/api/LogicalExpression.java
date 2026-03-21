package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;

import java.util.Objects;

public final class LogicalExpression {

    private final ExpressionRuntimeSupport runtime;

    private LogicalExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    public static LogicalExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    public static LogicalExpression compile(String source, ExpressionEnvironment environment) {
        return new LogicalExpression(ExpressionRuntimeSupport.compileLogical(source, environment));
    }

    public static ValidationResult validate(String source) {
        return validate(source, ExpressionEnvironmentBuilder.empty());
    }

    public static ValidationResult validate(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validateLogical(source, environment);
    }

    public LogicalExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public boolean compute() {
        return runtime.computeLogical();
    }

    public AuditResult<Boolean> computeWithAudit() {
        return runtime.computeLogicalWithAudit();
    }
}
