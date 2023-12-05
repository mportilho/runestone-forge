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
    private ConcurrentMap<T, Future<R>> cache;
    private final ReentrantLock lock = new ReentrantLock();
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
