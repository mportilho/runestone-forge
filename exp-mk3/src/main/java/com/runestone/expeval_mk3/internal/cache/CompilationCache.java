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
        this.cache = newBuilder(config, MonotonicTicker.SYSTEM).build();
    }

    /**
     * Test-only seam: lets a suite advance expiration deterministically with a fake {@link MonotonicTicker}
     * instead of sleeping real time.
     */
    public CompilationCache(
            CacheConfig config,
            BiFunction<String, ExpressionEnvironment, ExpressionCompilationResult> compiler,
            MonotonicTicker ticker) {
        Objects.requireNonNull(config, "config");
        this.compiler = Objects.requireNonNull(compiler, "compiler");
        Objects.requireNonNull(ticker, "ticker");
        this.cache = newBuilder(config, ticker).build();
    }

    /**
     * A direct executor keeps Caffeine's post-write maintenance (admission window bookkeeping, buffer
     * draining, eviction) inline on the calling thread. This module's compilation cache is small, bounded,
     * and never on {@code compute}'s hot path, so the alternative default, dispatching that maintenance
     * onto {@code ForkJoinPool.commonPool()}, buys no real concurrency here: it only adds cross-thread
     * wake-up latency, measured at several microseconds per operation, to every miss and eviction.
     */
    private static Caffeine<Object, Object> newBuilder(CacheConfig config, MonotonicTicker ticker) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(config.maximumEntries())
                .ticker(ticker::read)
                .executor(Runnable::run);
        if (config.hasExpireAfterAccess()) {
            builder = builder.expireAfterAccess(config.expireAfterAccess());
        }
        return builder;
    }

    public ExpressionCompilationResult get(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        return cache.get(key, ignoredKey -> compiler.apply(source, environment));
    }

    /**
     * Test/benchmark-only seam: ends the resident generation for one key so a subsequent {@link #get}
     * observes a miss, without exposing invalidation on {@code ExpressionEngine}'s public API.
     */
    public void invalidate(String source, ExpressionEnvironment environment) {
        cache.invalidate(new CompilationCacheKey(source, environment.environmentId()));
    }
}
