package com.runestone.expeval_mk3.internal.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval_mk3.api.CacheConfig;
import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * The single Caffeine-backed cache owned by one {@code ExpressionEngine}. The key combines the exact
 * source and the Environment Instance Identifier; the value is the complete
 * {@link ExpressionCompilationResult}, success or expected failure alike. Caffeine's atomic
 * {@code get(key, mappingFunction)} gives single-flight per key and generation: concurrent callers for
 * the same key run the compiler exactly once and share its result. A compiler exception escapes without
 * installing an entry, so a later call may retry.
 */
public final class CompilationCache {

    private final Cache<CompilationCacheKey, ExpressionCompilationResult> cache;
    private final BiFunction<String, ExpressionEnvironment, ExpressionCompilationResult> compiler;

    public CompilationCache(
            CacheConfig config, BiFunction<String, ExpressionEnvironment, ExpressionCompilationResult> compiler) {
        Objects.requireNonNull(config, "config");
        this.compiler = Objects.requireNonNull(compiler, "compiler");
        Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(config.maximumEntries());
        if (config.hasExpireAfterAccess()) {
            builder = builder.expireAfterAccess(config.expireAfterAccess());
        }
        this.cache = builder.build();
    }

    public ExpressionCompilationResult get(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        return cache.get(key, ignoredKey -> compiler.apply(source, environment));
    }
}
