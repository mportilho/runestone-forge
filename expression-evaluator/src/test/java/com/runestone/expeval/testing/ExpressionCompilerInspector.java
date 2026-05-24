package com.runestone.expeval.testing;

import com.runestone.expeval.compiler.ExpressionCompiler;
import com.runestone.expeval.compiler.ExpressionCompilerAccess;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.compiler.CompiledExpression;
import com.runestone.expeval.internal.grammar.ExpressionResultType;

import java.util.Objects;

/**
 * Test-only access point for white-box compilation assertions.
 */
public final class ExpressionCompilerInspector {

    private final ExpressionCompiler compiler;

    public ExpressionCompilerInspector() {
        this(new ExpressionCompiler());
    }

    public ExpressionCompilerInspector(ExpressionCompiler compiler) {
        this.compiler = Objects.requireNonNull(compiler, "compiler must not be null");
    }

    public CompiledExpression compileMath(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.MATH, environment);
    }

    public CompiledExpression compileLogical(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.LOGICAL, environment);
    }

    public CompiledExpression compileAssignments(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    public CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        return ExpressionCompilerAccess.compile(compiler, source, resultType, environment);
    }
}
