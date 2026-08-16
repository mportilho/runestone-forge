package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.cache.CompilationCache;
import com.runestone.expeval_mk3.internal.parser.ParserWarmUp;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;

import java.time.Clock;
import java.util.Objects;

/**
 * The single public entry point for compilation. An engine owns its {@link RuntimeServices}, including
 * the {@link Clock} used for current temporal values, and one limited compilation cache: calls for the
 * same source text and the same Environment Instance Identifier share a resident
 * {@link ExpressionCompilationResult} instead of repeating parsing, semantic resolution, and Plano
 * Imutavel construction.
 *
 * <p>{@link #defaultEngine()} is a lazily initialized, thread-safe singleton using
 * {@link CacheConfig#defaults()} and {@link Clock#systemUTC()}. {@link #builder()} creates isolated
 * engines with their own cache configuration and clock; engines never share cache entries or
 * {@code RuntimeServices}. Building the first engine of either kind triggers the module's one-time
 * parser warm-up.
 */
public final class ExpressionEngine {

    private final CompilationCache cache;

    private ExpressionEngine(CacheConfig cacheConfig, Clock clock) {
        ParserWarmUp.shared().ensureWarmedUp();
        RuntimeServices runtimeServices = RuntimeServices.withClock(clock);
        this.cache = new CompilationCache(
                cacheConfig, (source, environment) -> CompilationPipeline.compile(source, environment, runtimeServices));
    }

    public static ExpressionEngine defaultEngine() {
        return DefaultEngineHolder.INSTANCE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ExpressionCompilationResult compile(String source, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(environment, "environment");
        return cache.get(source, environment);
    }

    public CompiledExpression compileOrThrow(String source, ExpressionEnvironment environment) {
        ExpressionCompilationResult result = compile(source, environment);
        if (result instanceof ExpressionCompilationResult.Failure failure) {
            throw new ExpressionCompilationException(failure.diagnostics());
        }
        return ((ExpressionCompilationResult.Success) result).compiledExpression();
    }

    /** Test/benchmark-only seam; see {@link CompilationCache#invalidate}. */
    CompilationCache cache() {
        return cache;
    }

    /** Deferred so the default engine, and its warm-up, is built only when first requested. */
    private static final class DefaultEngineHolder {
        private static final ExpressionEngine INSTANCE = new ExpressionEngine(CacheConfig.defaults(), Clock.systemUTC());
    }

    public static final class Builder {

        private CacheConfig cacheConfig = CacheConfig.defaults();
        private Clock clock = Clock.systemUTC();

        private Builder() {
        }

        public Builder cacheConfig(CacheConfig cacheConfig) {
            this.cacheConfig = Objects.requireNonNull(cacheConfig, "cacheConfig");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public ExpressionEngine build() {
            return new ExpressionEngine(cacheConfig, clock);
        }
    }
}
