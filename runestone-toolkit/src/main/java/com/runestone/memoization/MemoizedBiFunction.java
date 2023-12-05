package com.runestone.memoization;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

/**
 * A {@link BiFunction} implementation that caches the result of a delegate function and returns the cached value on
 * subsequent calls. This implementation is thread-safe and uses a {@link ConcurrentHashMap} to store the cached values. If
 * an exception is thrown by the delegate function, the exception is rethrown and the cache is not updated.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of results supplied by this supplier
 */
public class MemoizedBiFunction<T, U, R> implements BiFunction<T, U, R> {

    private final BiFunction<T, U, R> delegate;
    private ConcurrentMap<BiParameter, Future<R>> cache;
    private final boolean retryOnError;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Creates a new {@link MemoizedBiFunction} instance that caches the result of the delegate function.
     *
     * @param delegate the delegate function
     */
    MemoizedBiFunction(BiFunction<T, U, R> delegate) {
        this(delegate, true);
    }

    /**
     * Creates a new {@link MemoizedBiFunction} instance that caches the result of the delegate function.
     *
     * @param delegate     the delegate function
     * @param retryOnError whether to retry the delegate function call if an exception is thrown
     */
    MemoizedBiFunction(BiFunction<T, U, R> delegate, boolean retryOnError) {
        this.delegate = delegate;
        this.retryOnError = retryOnError;
    }

    @Override
    public R apply(T t, U u) {
        if (cache == null) {
            lock.lock();
            try {
                if (cache == null) {
                    cache = new ConcurrentHashMap<>(128);
                }
            } finally {
                lock.unlock();
            }
        }
        try {
            return applyAsync(t, u);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Memoization interrupted while fetching a new value", e);
        }
    }

    private R applyAsync(T t, U u) throws InterruptedException {
        BiParameter biParameter = new BiParameter(t, u);
        Future<R> future = cache.get(biParameter);
        if (future == null) {
            final Callable<R> eval = () -> delegate.apply(t, u);
            final FutureTask<R> futureTask = new FutureTask<>(eval);
            future = cache.putIfAbsent(biParameter, futureTask);
            if (future == null) {
                future = futureTask;
                futureTask.run();
            }
        }
        try {
            return future.get();
        } catch (CancellationException e) {
            cache.remove(biParameter, future);
        } catch (ExecutionException e) {
            if (retryOnError) {
                cache.remove(biParameter, future);
            }
            Throwable throwable = e.getCause();
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

    private record BiParameter(Object p1, Object p2) {
    }
}
