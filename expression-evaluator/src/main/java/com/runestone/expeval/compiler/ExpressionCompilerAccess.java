package com.runestone.expeval.compiler;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.runtime.CompiledExpression;

import java.util.Objects;

/**
 * Internal bridge for runtime and test-support code that needs the compiled representation.
 *
 * <p>Application code should use {@code MathExpression}, {@code LogicalExpression}, or
 * {@code AssignmentExpression}. This bridge exists because the public compiler facade and the
 * runtime support currently live in different packages.
 */
public final class ExpressionCompilerAccess {

    private ExpressionCompilerAccess() {
    }

    public static CompiledExpression compile(
            ExpressionCompiler compiler,
            String source,
            ExpressionResultType resultType,
            ExpressionEnvironment environment) {
        Objects.requireNonNull(compiler, "compiler must not be null");
        return compiler.compile(source, resultType, environment);
    }
}
