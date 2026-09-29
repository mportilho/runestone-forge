package com.runestone.expeval.internal.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.api.ExpressionCompilationResult;
import com.runestone.expeval.api.ExpressionEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
    private final InFlightStripe[] inFlightStripes = newInFlightStripes();

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
            InFlightStripe stripe = inFlightStripe(key);
            long observedCompletion = stripe.completionVersion;
            CachedCompilation resident = cache.getIfPresent(key);
            if (resident != null) {
                return resident.result();
            }

            InFlightCompilation inFlight;
            boolean leader;
            boolean waitForInvalidatedGeneration;
            synchronized (stripe) {
                if (stripe.completionVersion != observedCompletion) {
                    resident = cache.getIfPresent(key);
                    if (resident != null) {
                        return resident.result();
                    }
                }
                inFlight = stripe.get(key);
                waitForInvalidatedGeneration = inFlight != null && inFlight.isInvalidated();
                if (inFlight == null) {
                    inFlight = stripe.newCompilation();
                    stripe.put(key, inFlight);
                    leader = true;
                } else {
                    leader = false;
                }
                if (!waitForInvalidatedGeneration) {
                    inFlight.join(leader);
                }
            }

            if (waitForInvalidatedGeneration) {
                inFlight.awaitFinished();
                continue;
            }

            if (!leader) {
                return inFlight.await();
            }
            try {
                CachedCompilation compiled = compiler.apply(source, environment);
                ExpressionCompilationResult result = compiled.result();
                synchronized (stripe) {
                    if (!inFlight.isInvalidated()) {
                        cache.put(key, compiled);
                        stripe.completionVersion++;
                    }
                    inFlight.succeed(result);
                    stripe.remove(key, inFlight);
                    inFlight.finish();
                }
                return result;
            } catch (RuntimeException | Error exception) {
                synchronized (stripe) {
                    inFlight.fail(exception);
                    stripe.remove(key, inFlight);
                    inFlight.finish();
                }
                throw exception;
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
        InFlightStripe stripe = inFlightStripe(key);
        synchronized (stripe) {
            cache.invalidate(key);
            inFlight = stripe.get(key);
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
        InFlightStripe stripe = inFlightStripe(key);
        synchronized (stripe) {
            InFlightCompilation inFlight = stripe.get(key);
            return inFlight == null ? 0 : inFlight.participantCount();
        }
    }

    /** Test-only seam for observing that invalidation has ended admission for an active generation. */
    boolean isInFlightInvalidated(String source, ExpressionEnvironment environment) {
        CompilationCacheKey key = new CompilationCacheKey(source, environment.environmentId());
        InFlightStripe stripe = inFlightStripe(key);
        synchronized (stripe) {
            InFlightCompilation inFlight = stripe.get(key);
            return inFlight != null && inFlight.isInvalidated();
        }
    }

    private static InFlightStripe[] newInFlightStripes() {
        InFlightStripe[] stripes = new InFlightStripe[IN_FLIGHT_STRIPE_COUNT];
        for (int index = 0; index < stripes.length; index++) {
            stripes[index] = new InFlightStripe();
        }
        return stripes;
    }

    private InFlightStripe inFlightStripe(CompilationCacheKey key) {
        return inFlightStripes[key.hashCode() & (IN_FLIGHT_STRIPE_COUNT - 1)];
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

        private CompletableFuture<ExpressionCompilationResult> resultSignal;
        private CompletableFuture<Void> finishedSignal;
        private int participants;
        private boolean invalidated;

        private void join(boolean leader) {
            participants++;
            if (!leader && resultSignal == null) {
                resultSignal = new CompletableFuture<>();
            }
        }

        private void succeed(ExpressionCompilationResult compilationResult) {
            CompletableFuture<ExpressionCompilationResult> signal = resultSignal;
            if (signal != null) {
                signal.complete(compilationResult);
            }
        }

        private void fail(Throwable exception) {
            CompletableFuture<ExpressionCompilationResult> signal = resultSignal;
            if (signal != null) {
                signal.completeExceptionally(exception);
            }
        }

        private ExpressionCompilationResult await() {
            try {
                return resultSignal.join();
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
            if (finishedSignal == null) {
                finishedSignal = new CompletableFuture<>();
            }
        }

        private boolean isInvalidated() {
            return invalidated;
        }

        private void finish() {
            CompletableFuture<Void> signal = finishedSignal;
            if (signal != null) {
                signal.complete(null);
            }
        }

        private void awaitFinished() {
            finishedSignal.join();
        }

        private boolean reusable() {
            return participants == 1 && resultSignal == null && finishedSignal == null;
        }

        private void reset() {
            participants = 0;
            invalidated = false;
        }
    }

    /**
     * Allocation-free common-case flight slot guarded by the stripe monitor. Hash-map storage is created
     * only when unrelated active keys collide in the same stripe.
     */
    private static final class InFlightStripe {

        private CompilationCacheKey primaryKey;
        private InFlightCompilation primary;
        private Map<CompilationCacheKey, InFlightCompilation> collisions;
        private InFlightCompilation reusableCompilation;
        private volatile long completionVersion;

        private InFlightCompilation get(CompilationCacheKey key) {
            if (key.equals(primaryKey)) {
                return primary;
            }
            return collisions == null ? null : collisions.get(key);
        }

        private void put(CompilationCacheKey key, InFlightCompilation compilation) {
            if (primary == null) {
                primaryKey = key;
                primary = compilation;
                return;
            }
            if (collisions == null) {
                collisions = new HashMap<>();
            }
            collisions.put(key, compilation);
        }

        private InFlightCompilation newCompilation() {
            InFlightCompilation compilation = reusableCompilation;
            if (compilation == null) {
                return new InFlightCompilation();
            }
            reusableCompilation = null;
            compilation.reset();
            return compilation;
        }

        private void remove(CompilationCacheKey key, InFlightCompilation compilation) {
            if (compilation == primary && key.equals(primaryKey)) {
                primaryKey = null;
                primary = null;
            } else if (collisions != null) {
                collisions.remove(key, compilation);
            }
            if (compilation.reusable() && reusableCompilation == null) {
                reusableCompilation = compilation;
            }
        }
    }
}
