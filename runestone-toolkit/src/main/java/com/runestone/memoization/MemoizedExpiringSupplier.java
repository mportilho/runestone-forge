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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A {@link Supplier} implementation that caches the result of a delegate supplier and returns the cached value on
 * subsequent calls. The cache expires after a given duration. This implementation is thread-safe and uses a
 * {@link ReentrantLock} to synchronize the access to the cache.
 *
 * @param <T>
 * @author Marcelo Portilho
 */
public class MemoizedExpiringSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private transient T cache;
    private final long durationNanos;
    private transient volatile long expirationNanos;
    private volatile ReentrantLock reentrantLock;

    /**
     * Creates a new {@link MemoizedExpiringSupplier} instance that caches the result of the delegate supplier for the
     * given duration.
     *
     * @param delegate the delegate supplier
     * @param duration the duration
     * @param timeUnit the time unit of the duration
     */
    MemoizedExpiringSupplier(Supplier<T> delegate, long duration, TimeUnit timeUnit) {
        if (duration <= 0) {
            throw new IllegalArgumentException(String.format("Provided duration (%s %s) must be greater than zero", duration, timeUnit));
        }
        this.delegate = delegate;
        this.durationNanos = timeUnit.toNanos(duration);
    }

    @Override
    public T get() {
        long nanos = expirationNanos;
        long now = System.nanoTime();
        if (nanos == 0 || now - nanos >= 0) {
            ReentrantLock lock = getReentrantLock();
            lock.lock();
            try {
                if (nanos == expirationNanos) {
                    T t = delegate.get();
                    cache = t;
                    nanos = now + durationNanos;
                    expirationNanos = (nanos == 0) ? 1 : nanos;
                    return t;
                }
            } finally {
                lock.unlock();
            }
        }
        return cache;
    }

    private ReentrantLock getReentrantLock() {
        if (reentrantLock == null) {
            synchronized (this) {
                if (reentrantLock == null) {
                    reentrantLock = new ReentrantLock();
                }
            }
        }
        return reentrantLock;
    }

}
