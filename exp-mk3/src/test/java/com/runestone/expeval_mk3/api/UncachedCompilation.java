package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

/**
 * Test/benchmark bridge to the package-private {@link CompilationPipeline}, so callers outside the
 * {@code api} package (JMH) can measure the real uncached seam instead of {@link ExpressionCompiler}.
 */
public final class UncachedCompilation {

    private UncachedCompilation() {
    }

    public static ExpressionCompilationResult compile(String source, ExpressionEnvironment environment) {
        return CompilationPipeline.compile(source, environment, RuntimeServices.systemDefault());
    }
}
