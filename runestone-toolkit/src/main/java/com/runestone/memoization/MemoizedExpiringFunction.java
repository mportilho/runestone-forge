package com.runestone.memoization;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * A {@link Function} implementation that caches the result of a delegate function and returns the cached value on
 * subsequent calls. This implementation is thread-safe and uses a {@link ConcurrentHashMap} to store the cached values.
 * The cache expires after a given duration.
 *
 * @param <T> the type of arguments to the function
 * @param <R> the type of results supplied by this supplier
 */
public class MemoizedExpiringFunction<T, R> implements Function<T, R> {

    private final Function<T, R> delegate;
    private final ConcurrentMap<T, CacheEntry<R>> cache = new ConcurrentHashMap<>(128);
    private final long durationMillis;
    private final boolean retryOnError;

    /**
     * Creates a new {@link MemoizedExpiringFunction} instance.
     *
     * @param delegate     the delegate function to be memoized
     * @param duration     the duration of the cache
     * @param timeUnit     the time unit of the duration
     * @param retryOnError whether to retry the delegate function on error
     */
    public MemoizedExpiringFunction(Function<T, R> delegate, long duration, TimeUnit timeUnit, boolean retryOnError) {
        this.delegate = Objects.requireNonNull(delegate, "Function delegate must be provided");
        if (duration <= 0) {
            throw new IllegalArgumentException("Provided duration must be greater than zero");
        }
        this.durationMillis = timeUnit.toMillis(duration);
        this.retryOnError = retryOnError;
    }

    /**
     * Creates a new {@link MemoizedExpiringFunction} instance.
     *
     * @param delegate the delegate function to be memoized
     * @param duration the duration of the cache
     * @param timeUnit the time unit of the duration
     */
    public MemoizedExpiringFunction(Function<T, R> delegate, long duration, TimeUnit timeUnit) {
        this(delegate, duration, timeUnit, true);
    }

    @Override
    public R apply(T t) {
        try {
            return applyWithExpiry(t);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Memoization interrupted while fetching a new value", e);
        }
    }

    private R applyWithExpiry(T t) throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        CacheEntry<R> entry = cache.get(t);

        if (entry == null || entry.isExpired(currentTime)) {
            Callable<R> eval = () -> delegate.apply(t);
            FutureTask<R> futureTask = new FutureTask<>(eval);
            entry = new CacheEntry<>(futureTask, currentTime + durationMillis);
            cache.put(t, entry);
            futureTask.run();
        }
        try {
            return entry.future().get();
        } catch (CancellationException e) {
            cache.remove(t, entry);
        } catch (ExecutionException e) {
            Throwable throwable = e.getCause();
            if (retryOnError) {
                cache.remove(t, entry);
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
}
