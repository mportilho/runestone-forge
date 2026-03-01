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
        long currentTime = System.currentTimeMillis();
        CacheEntry<R> entry = cache.get(t);

        if (entry == null || entry.isExpired(currentTime)) {
            entry = cache.compute(t, (key, currentEntry) -> {
                if (currentEntry == null || currentEntry.isExpired(currentTime)) {
                    FutureTask<R> futureTask = new FutureTask<>(() -> delegate.apply(key));
                    return new CacheEntry<>(futureTask, currentTime + durationMillis);
                }
                return currentEntry;
            });
            
            // If the future hasn't started yet, run it. ConcurrentHashMap.compute is atomic.
            // We check if the future is a FutureTask to run it once.
            if (entry.future instanceof FutureTask<R> task) {
                task.run();
            }
        }
        
        try {
            return entry.future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Memoization interrupted while fetching a new value", e);
        } catch (CancellationException e) {
            cache.remove(t, entry);
            throw new IllegalStateException("Memoization cancelled while fetching a new value", e);
        } catch (ExecutionException e) {
            if (retryOnError) {
                cache.remove(t, entry);
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

    private record CacheEntry<R>(Future<R> future, long expirationTime) {
        boolean isExpired(long currentTime) {
            return currentTime >= expirationTime;
        }
    }
}
