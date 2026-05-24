package com.runestone.expeval.internal.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentId;
import com.runestone.expeval.internal.grammar.ExpressionResultType;

import java.util.Objects;

/**
 * Internal cache boundary for compiled expressions.
 */
public final class ExpressionCompilationCache {

    private final ExpressionCompiler compiler;
    private final Cache<ExpressionCacheKey, CompiledExpression> cache;

    public ExpressionCompilationCache(CacheConfig cacheConfig) {
        this(new ExpressionCompiler(), cacheConfig);
    }

    ExpressionCompilationCache(ExpressionCompiler compiler, CacheConfig cacheConfig) {
        this.compiler = Objects.requireNonNull(compiler, "compiler must not be null");
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().maximumSize(cacheConfig.maximumSize());
        if (cacheConfig.expireAfterWrite() != null) {
            caffeine.expireAfterWrite(cacheConfig.expireAfterWrite());
        }
        this.cache = caffeine.build();
    }

    public CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        ExpressionCacheKey cacheKey = new ExpressionCacheKey(source, environment.environmentId(), resultType);
        return cache.get(cacheKey, ignored -> compiler.compile(source, resultType, environment));
    }

    public void invalidateCache() {
        cache.invalidateAll();
    }

    private record ExpressionCacheKey(
            String source,
            ExpressionEnvironmentId environmentId,
            ExpressionResultType resultType
    ) {

        public ExpressionCacheKey {
            if (source == null || source.isBlank()) {
                throw new IllegalArgumentException("source must not be blank");
            }
            Objects.requireNonNull(environmentId, "environmentId must not be null");
            Objects.requireNonNull(resultType, "resultType must not be null");
        }
    }
}
