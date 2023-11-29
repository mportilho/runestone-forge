package com.runestone.memoization;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiFunction;

/**
 * A {@link BiFunction} implementation that caches the result of a delegate function and returns the cached value on
 * subsequent calls. This implementation is thread-safe and uses a {@link ConcurrentHashMap} to store the cached values. The
 * cache expires after a given duration.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of results supplied by this supplier
 * @author Marcelo Portilho
 */
public class MemoizedExpiringBiFunction<T, U, R> implements BiFunction<T, U, R> {

    private final BiFunction<T, U, R> delegate;
    private final ConcurrentMap<BiParameter, CacheEntry<R>> cache = new ConcurrentHashMap<>(128);
    private final long durationMillis;
    private final boolean retryOnError;

    /**
     * Creates a new {@link MemoizedExpiringBiFunction} instance.
     *
     * @param delegate     the delegate function to be memoized
     * @param duration     the duration of the cache
     * @param timeUnit     the time unit of the duration
     * @param retryOnError whether to retry the delegate function on error
     */
    public MemoizedExpiringBiFunction(BiFunction<T, U, R> delegate, long duration, TimeUnit timeUnit, boolean retryOnError) {
        this.delegate = Objects.requireNonNull(delegate, "Function delegate must be provided");
        if (duration <= 0) {
            throw new IllegalArgumentException("Provided duration must be greater than zero");
        }
        this.durationMillis = timeUnit.toMillis(duration);
        this.retryOnError = retryOnError;
    }

    /**
     * Creates a new {@link MemoizedExpiringBiFunction} instance.
     *
     * @param delegate the delegate function to be memoized
     * @param duration the duration of the cache
     * @param timeUnit the time unit of the duration
     */
    public MemoizedExpiringBiFunction(BiFunction<T, U, R> delegate, long duration, TimeUnit timeUnit) {
        this(delegate, duration, timeUnit, true);
    }

    @Override
    public R apply(T t, U u) {
        try {
            return applyAsync(t, u);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Memoization interrupted while fetching a new value", e);
        }
    }

    private R applyAsync(T t, U u) throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        BiParameter biParameter = new BiParameter(t, u);
        CacheEntry<R> entry = cache.get(biParameter);

        if (entry == null || entry.isExpired(currentTime)) {
            Callable<R> eval = () -> delegate.apply(t, u);
            FutureTask<R> futureTask = new FutureTask<>(eval);
            entry = new CacheEntry<>(futureTask, currentTime + durationMillis);
            cache.put(biParameter, entry);
            futureTask.run();
        }
        try {
            return entry.future().get();
        } catch (CancellationException e) {
            cache.remove(biParameter, entry);
        } catch (ExecutionException e) {
            Throwable throwable = e.getCause();
            if (retryOnError) {
                cache.remove(biParameter, entry);
            }
            if (throwable instanceof RuntimeException exception) {
                throw exception;
            } else if (throwable instanceof Error error) {
                throw error;
            } else {
                throw new IllegalStateException("Error while fetching new value for memoized function", throwable);
            }
        }
        throw new IllegalStateException("Error while fetching new value for memoized function");
    }

    private record CacheEntry<R>(FutureTask<R> future, long expirationTime) {
        boolean isExpired(long currentTime) {
            return currentTime >= expirationTime;
        }
    }

    private record BiParameter(Object p1, Object p2) {
    }
}
