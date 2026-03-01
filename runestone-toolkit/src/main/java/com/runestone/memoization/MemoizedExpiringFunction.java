/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
package com.runestone.memoization;
 
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
 
/**
 * A {@link Function} implementation that caches the result of a delegate function and returns the cached value on
 * subsequent calls. This implementation is thread-safe and uses Caffeine to store the cached values.
 * The cache expires after a given duration.
 *
 * @param <T> the type of arguments to the function
 * @param <R> the type of results supplied by this supplier
 */
public class MemoizedExpiringFunction<T, R> implements Function<T, R> {
 
    private final Function<T, R> delegate;
    private final ConcurrentMap<T, Future<R>> cache;
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
        this(delegate, 2048, duration, timeUnit, retryOnError);
    }

    /**
     * Creates a new {@link MemoizedExpiringFunction} instance.
     *
     * @param delegate     the delegate function to be memoized
     * @param maximumSize  the maximum number of entries the cache can hold
     * @param duration     the duration of the cache
     * @param timeUnit     the time unit of the duration
     * @param retryOnError whether to retry the delegate function on error
     */
    public MemoizedExpiringFunction(Function<T, R> delegate, long maximumSize, long duration, TimeUnit timeUnit, boolean retryOnError) {
        this.delegate = Objects.requireNonNull(delegate, "Function delegate must be provided");
        if (duration <= 0) {
            throw new IllegalArgumentException("Provided duration must be greater than zero");
        }
        this.retryOnError = retryOnError;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(duration, timeUnit)
                .executor(Runnable::run)
                .<T, Future<R>>build().asMap();
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
        Future<R> future = cache.get(t);
        try {
            if (future == null) {
                FutureTask<R> futureTask = new FutureTask<>(() -> delegate.apply(t));
                future = cache.putIfAbsent(t, futureTask);
                if (future == null) {
                    future = futureTask;
                    futureTask.run();
                }
            }
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Memoization interrupted while fetching a new value", e);
        } catch (CancellationException e) {
            if (future != null) {
                cache.remove(t, future);
            }
            throw new IllegalStateException("Memoization cancelled while fetching a new value", e);
        } catch (ExecutionException e) {
            if (retryOnError && future != null) {
                cache.remove(t, future);
            }
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException exception) {
                throw exception;
            } else if (cause instanceof Error error) {
                throw error;
            } else {
                throw new IllegalStateException("Error while fetching new value for memoized function", cause);
            }
        }
    }
}
