package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

/**
 * Temporary public facade delegating to {@link CompilationPipeline}; it will be replaced by the
 * Engine de Expressao once compilation gains a cache (issue #131).
 */
public final class ExpressionCompiler {

    private ExpressionCompiler() {
    }

    public static ExpressionCompilationResult compile(String source, ExpressionEnvironment environment) {
        return compile(source, environment, RuntimeServices.systemDefault());
    }

    public static CompiledExpression compileOrThrow(String source, ExpressionEnvironment environment) {
        return compileOrThrow(source, environment, RuntimeServices.systemDefault());
    }

    /** Internal seam letting tests inject a fixed or counting {@link java.time.Clock}. */
    static CompiledExpression compileOrThrow(
            String source, ExpressionEnvironment environment, RuntimeServices runtimeServices) {
        ExpressionCompilationResult result = compile(source, environment, runtimeServices);
        if (result instanceof ExpressionCompilationResult.Failure failure) {
            throw new ExpressionCompilationException(failure.diagnostics());
        }
        return ((ExpressionCompilationResult.Success) result).compiledExpression();
    }

    /** Internal seam letting tests inject a fixed or counting {@link java.time.Clock}. */
    static ExpressionCompilationResult compile(
            String source, ExpressionEnvironment environment, RuntimeServices runtimeServices) {
        return CompilationPipeline.compile(source, environment, runtimeServices);
    }
}
