package com.runestone.expeval.compiler;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.compiler.CompiledExpression;
import com.runestone.expeval.internal.compiler.DefaultExpressionCompiler;
import com.runestone.expeval.internal.grammar.ExpressionResultType;

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

    CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        return delegate.compile(source, resultType, environment);
    }

    /**
     * Removes all entries from this compiler's compilation cache.
     */
    public void invalidateCache() {
        delegate.invalidateCache();
    }
}
