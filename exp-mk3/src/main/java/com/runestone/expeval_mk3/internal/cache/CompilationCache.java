package com.runestone.expeval_mk3.internal.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval_mk3.api.CacheConfig;
import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * The single Caffeine-backed cache owned by one {@code ExpressionEngine}. The key combines the exact
 * source and the Environment Instance Identifier; the value is the complete
 * {@link ExpressionCompilationResult}, success or expected failure alike. A short-lived in-flight handoff and
 * an explicit per-key flight give single-flight per generation: concurrent callers for the same key run the
 * compiler exactly once and share its result, even when an overweight entry is immediately rejected from
 * residency. Admission is serialized with invalidation so an invalidated flight is never briefly observable
 * as resident. One Caffeine {@code maximumWeight} combines the exact entry limit with conservative
 * source-controlled retained weight. A compiler exception escapes without installing an entry, so a later
 * call may retry.
 */
public final class CompilationCache {

    private static final int IN_FLIGHT_STRIPE_COUNT = 64;

    private final Cache<CompilationCacheKey, CachedCompilation> cache;
    private final BiFunction<String, ExpressionEnvironment, CachedCompilation> compiler;
    private final Object[] inFlightLocks = newInFlightLocks();
    private final Map<CompilationCacheKey, InFlightCompilation> inFlightCompilations = new ConcurrentHashMap<>();

    public CompilationCache(
            CacheConfig config, BiFunction<String, ExpressionEnvironment, CachedCompilation> compiler) {
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
            BiFunction<String, ExpressionEnvironment, CachedCompilation> compiler,
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
        int minimumEntryQuota = minimumEntryQuota(config);
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.maximumWeight(config.maximumRetainedWeight());
        builder.weigher((key, result) -> {
            CachedCompilation compilation = (CachedCompilation) result;
            return Math.max(compilation.retainedWeight(), minimumEntryQuota);
        });
        builder.ticker(ticker::read);
        builder.executor(Runnable::run);
        if (config.hasExpireAfterAccess()) {
            builder = builder.expireAfterAccess(config.expireAfterAccess());
        }
        return builder;
    }

    private static int minimumEntryQuota(CacheConfig config) {
        long maximumWeight = config.maximumRetainedWeight();
        return (int) ((maximumWeight - 1) / config.maximumEntries() + 1);
    }

    public ExpressionCompilationResult get(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        while (true) {
            CachedCompilation resident = cache.getIfPresent(key);
            if (resident != null) {
                return resident.result();
            }

            InFlightCompilation inFlight;
            boolean leader;
            boolean waitForInvalidatedGeneration;
            Object inFlightLock = inFlightLock(key);
            synchronized (inFlightLock) {
                resident = cache.getIfPresent(key);
                if (resident != null) {
                    return resident.result();
                }
                inFlight = inFlightCompilations.get(key);
                waitForInvalidatedGeneration = inFlight != null && inFlight.isInvalidated();
                if (inFlight == null) {
                    inFlight = new InFlightCompilation();
                    inFlightCompilations.put(key, inFlight);
                    leader = true;
                } else {
                    leader = false;
                }
                if (!waitForInvalidatedGeneration) {
                    inFlight.join();
                }
            }

            if (waitForInvalidatedGeneration) {
                inFlight.awaitFinished();
                continue;
            }

            try {
                if (!leader) {
                    return inFlight.await();
                }
                CachedCompilation compiled = compiler.apply(source, environment);
                ExpressionCompilationResult result = compiled.result();
                synchronized (inFlightLock) {
                    if (!inFlight.isInvalidated()) {
                        cache.put(key, compiled);
                    }
                    inFlight.succeed(result);
                }
                return result;
            } catch (RuntimeException | Error exception) {
                inFlight.fail(exception);
                throw exception;
            } finally {
                if (leader) {
                    synchronized (inFlightLock) {
                        inFlightCompilations.remove(key, inFlight);
                    }
                    inFlight.finish();
                }
            }
        }
    }

    /**
     * Test/benchmark-only seam: ends the resident generation for one key so a subsequent {@link #get}
     * observes a miss, without exposing invalidation on {@code ExpressionEngine}'s public API. An active
     * compilation may still be delivered to its joined callers, but cannot become resident after invalidation.
     */
    public void invalidate(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        InFlightCompilation inFlight;
        Object inFlightLock = inFlightLock(key);
        synchronized (inFlightLock) {
            cache.invalidate(key);
            inFlight = inFlightCompilations.get(key);
            if (inFlight != null) {
                inFlight.invalidate();
            }
        }
        if (inFlight != null) {
            inFlight.awaitFinished();
        }
    }

    /** Test-only seam for deterministically coordinating callers already joined to one miss. */
    int inFlightParticipantCount(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        synchronized (inFlightLock(key)) {
            InFlightCompilation inFlight = inFlightCompilations.get(key);
            return inFlight == null ? 0 : inFlight.participantCount();
        }
    }

    /** Test-only seam for observing that invalidation has ended admission for an active generation. */
    boolean isInFlightInvalidated(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        synchronized (inFlightLock(key)) {
            InFlightCompilation inFlight = inFlightCompilations.get(key);
            return inFlight != null && inFlight.isInvalidated();
        }
    }

    private static Object[] newInFlightLocks() {
        Object[] locks = new Object[IN_FLIGHT_STRIPE_COUNT];
        for (int index = 0; index < locks.length; index++) {
            locks[index] = new Object();
        }
        return locks;
    }

    private Object inFlightLock(CompilationCacheKey key) {
        return inFlightLocks[key.hashCode() & (IN_FLIGHT_STRIPE_COUNT - 1)];
    }

    /** Compiler result paired with its one-time admission estimate. */
    public record CachedCompilation(ExpressionCompilationResult result, int retainedWeight) {

        public CachedCompilation {
            Objects.requireNonNull(result, "result");
            if (retainedWeight <= 0) {
                throw new IllegalArgumentException("retainedWeight must be positive: " + retainedWeight);
            }
        }
    }

    /** Result handoff that exists only while a miss is being compiled. */
    private static final class InFlightCompilation {

        private final CompletableFuture<ExpressionCompilationResult> result = new CompletableFuture<>();
        private final CompletableFuture<Void> finished = new CompletableFuture<>();
        private int participants;
        private boolean invalidated;

        private void join() {
            participants++;
        }

        private void succeed(ExpressionCompilationResult compilationResult) {
            result.complete(compilationResult);
        }

        private void fail(Throwable exception) {
            result.completeExceptionally(exception);
        }

        private ExpressionCompilationResult await() {
            try {
                return result.join();
            } catch (CompletionException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw exception;
            }
        }

        private int participantCount() {
            return participants;
        }

        private void invalidate() {
            invalidated = true;
        }

        private boolean isInvalidated() {
            return invalidated;
        }

        private void finish() {
            finished.complete(null);
        }

        private void awaitFinished() {
            finished.join();
        }
    }
}
