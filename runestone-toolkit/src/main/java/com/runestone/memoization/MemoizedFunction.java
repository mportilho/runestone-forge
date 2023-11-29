package com.runestone.memoization;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * A {@link Function} implementation that caches the result of a delegate function and returns the cached value on
 * subsequent calls. This implementation is thread-safe and uses a {@link ConcurrentHashMap} to store the cached values. If
 * an exception is thrown by the delegate function, the exception is rethrown and the cache is not updated.
 *
 * @param <T> the type of arguments to the function
 * @param <R> the type of results supplied by this supplier
 * @author Marcelo Portilho
 */
public class MemoizedFunction<T, R> implements Function<T, R> {

    private final Function<T, R> delegate;
    private final ConcurrentMap<T, Future<R>> cache = new ConcurrentHashMap<>(128);
    private final boolean retryOnError;

    /**
     * Creates a new {@link MemoizedFunction} instance that caches the result of the delegate function.
     *
     * @param delegate the delegate function
     */
    MemoizedFunction(Function<T, R> delegate) {
        this(delegate, true);
    }

    /**
     * Creates a new {@link MemoizedFunction} instance that caches the result of the delegate function.
     *
     * @param delegate     the delegate function
     * @param retryOnError whether to retry the delegate function call if an exception is thrown
     */
    MemoizedFunction(Function<T, R> delegate, boolean retryOnError) {
        this.delegate = Objects.requireNonNull(delegate, "Supplier delegate must be provided");
        this.retryOnError = retryOnError;
    }

    @Override
    public R apply(T t) {
        try {
            return applyAsync(t);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Memoization interrupted while fetching a new value", e);
        }
    }

    private R applyAsync(T t) throws InterruptedException {
        Future<R> future = cache.get(t);
        if (future == null) {
            final Callable<R> eval = () -> delegate.apply(t);
            final FutureTask<R> futureTask = new FutureTask<>(eval); // uses the current thread to execute the task
            future = cache.putIfAbsent(t, futureTask);
            if (future == null) {
                future = futureTask;
                futureTask.run();
            }
        }
        try {
            return future.get();
        } catch (CancellationException e) {
            cache.remove(t, future);
        } catch (ExecutionException e) {
            if (retryOnError) {
                cache.remove(t, future);
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

}
