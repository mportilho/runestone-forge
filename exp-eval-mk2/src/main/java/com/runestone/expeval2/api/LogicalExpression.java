package com.runestone.expeval2.api;

import com.runestone.expeval2.compiler.CompiledExpression;
import com.runestone.expeval2.compiler.ExpressionCompiler;
import com.runestone.expeval2.grammar.language.ExpressionResultType;
import com.runestone.expeval2.runtime.ExpressionRuntimeSupport;

import java.util.Objects;

public final class LogicalExpression {

    private static final ExpressionCompiler COMPILER = new ExpressionCompiler();

    private final ExpressionRuntimeSupport runtime;

    private LogicalExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    public static LogicalExpression compile(String source, ExpressionEnvironment environment) {
        CompiledExpression compiled = COMPILER.compile(source, ExpressionResultType.LOGICAL, environment);
        return new LogicalExpression(ExpressionRuntimeSupport.from(compiled, environment));
    }

    public LogicalExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public boolean compute() {
        return runtime.computeLogical();
    }
}
