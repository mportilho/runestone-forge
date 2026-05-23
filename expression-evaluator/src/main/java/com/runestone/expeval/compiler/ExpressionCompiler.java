package com.runestone.expeval.compiler;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.runtime.CompiledExpression;
import com.runestone.expeval.internal.runtime.DefaultExpressionCompiler;

import java.util.Objects;

/**
 * Public compiler facade for expression source strings.
 *
 * <p>The implementation remains internal; this type is the supported API for callers that need
 * an injectable compiler with an independently managed compilation cache.
 */
public class ExpressionCompiler {

    private final DefaultExpressionCompiler delegate;

    /**
     * Creates a compiler with the default cache configuration.
     */
    public ExpressionCompiler() {
        this(new DefaultExpressionCompiler());
    }

    /**
     * Creates a compiler with explicit cache parameters.
     *
     * @param cacheConfig cache settings; must not be {@code null}
     */
    public ExpressionCompiler(CacheConfig cacheConfig) {
        this(new DefaultExpressionCompiler(cacheConfig));
    }

    private ExpressionCompiler(DefaultExpressionCompiler delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    /**
     * Compiles {@code source} into a reusable expression plan.
     *
     * @param source      expression source text; must not be blank
     * @param resultType  expected result kind; must not be {@code null}
     * @param environment execution environment; must not be {@code null}
     * @return a compiled expression
     */
    public CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        return delegate.compile(source, resultType, environment);
    }

    /**
     * Removes all entries from this compiler's compilation cache.
     */
    public void invalidateCache() {
        delegate.invalidateCache();
    }
}
