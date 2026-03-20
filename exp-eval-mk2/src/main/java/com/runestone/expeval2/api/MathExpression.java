package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;

import java.math.BigDecimal;
import java.util.Objects;

public final class MathExpression {

    private final ExpressionRuntimeSupport runtime;

    private MathExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    public static MathExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    public static MathExpression compile(String source, ExpressionEnvironment environment) {
        return new MathExpression(ExpressionRuntimeSupport.compileMath(source, environment));
    }

    public MathExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public BigDecimal compute() {
        return runtime.computeMath();
    }

    public AuditResult<BigDecimal> computeWithAudit() {
        return runtime.computeMathWithAudit();
    }
}
