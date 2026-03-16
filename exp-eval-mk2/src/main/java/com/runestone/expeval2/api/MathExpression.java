package com.runestone.expeval2.api;

import com.runestone.expeval2.compiler.CompiledExpression;
import com.runestone.expeval2.compiler.ExpressionCompiler;
import com.runestone.expeval2.grammar.language.ExpressionResultType;
import com.runestone.expeval2.runtime.ExpressionRuntimeSupport;

import java.math.BigDecimal;
import java.util.Objects;

public final class MathExpression {

    private static final ExpressionCompiler COMPILER = new ExpressionCompiler();

    private final ExpressionRuntimeSupport runtime;

    private MathExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    public static MathExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    public static MathExpression compile(String source, ExpressionEnvironment environment) {
        CompiledExpression compiled = COMPILER.compile(source, ExpressionResultType.MATH, environment);
        return new MathExpression(ExpressionRuntimeSupport.from(compiled, environment));
    }

    public MathExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public BigDecimal compute() {
        return runtime.computeMath();
    }
}
