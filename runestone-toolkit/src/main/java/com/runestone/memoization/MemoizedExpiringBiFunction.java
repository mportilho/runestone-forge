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
import java.util.concurrent.locks.ReentrantLock;
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
    private ConcurrentMap<BiParameter, CacheEntry<R>> cache;
    private final long durationMillis;
    private final boolean retryOnError;
    private final ReentrantLock lock = new ReentrantLock();

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
