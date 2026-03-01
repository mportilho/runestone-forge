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
import java.util.function.BiFunction;
 
/**
 * A {@link BiFunction} implementation that caches the result of a delegate function and returns the cached value on
 * subsequent calls. This implementation is thread-safe and uses Caffeine to store the cached values. If
 * an exception is thrown by the delegate function, the exception is rethrown and the cache is not updated.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of results supplied by this supplier
 */
public class MemoizedBiFunction<T, U, R> implements BiFunction<T, U, R> {
 
    private final BiFunction<T, U, R> delegate;
    private final ConcurrentMap<BiParameter, Future<R>> cache;
    private final boolean retryOnError;
 
    /**
     * Creates a new {@link MemoizedBiFunction} instance that caches the result of the delegate function.
     *
     * @param delegate the delegate function
     */
    public MemoizedBiFunction(BiFunction<T, U, R> delegate) {
        this(delegate, true);
    }
 
    /**
     * Creates a new {@link MemoizedBiFunction} instance that caches the result of the delegate function.
     *
     * @param delegate     the delegate function
     * @param retryOnError whether to retry the delegate function call if an exception is thrown
     */
    public MemoizedBiFunction(BiFunction<T, U, R> delegate, boolean retryOnError) {
        this(delegate, 2048, retryOnError);
    }

    /**
     * Creates a new {@link MemoizedBiFunction} instance that caches the result of the delegate function.
     *
     * @param delegate     the delegate function
     * @param maximumSize  the maximum number of entries the cache can hold
     * @param retryOnError whether to retry the delegate function call if an exception is thrown
     */
    public MemoizedBiFunction(BiFunction<T, U, R> delegate, long maximumSize, boolean retryOnError) {
        this.delegate = Objects.requireNonNull(delegate, "Supplier delegate must be provided");
        this.retryOnError = retryOnError;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .executor(Runnable::run)
                .<BiParameter, Future<R>>build().asMap();
    }
 
    @Override
    public R apply(T t, U u) {
        BiParameter biParameter = new BiParameter(t, u);
        Future<R> future = cache.get(biParameter);
        try {
            if (future == null) {
                FutureTask<R> futureTask = new FutureTask<>(() -> delegate.apply(t, u));
                future = cache.putIfAbsent(biParameter, futureTask);
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
                cache.remove(biParameter, future);
            }
            throw new IllegalStateException("Memoization cancelled while fetching a new value", e);
        } catch (ExecutionException e) {
            if (retryOnError && future != null) {
                cache.remove(biParameter, future);
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
 
    private record BiParameter(Object p1, Object p2) {
    }
}
