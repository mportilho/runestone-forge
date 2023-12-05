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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A {@link Supplier} implementation that caches the result of a delegate supplier and returns the cached value on
 * subsequent calls.
 *
 * @param <T> the type of results supplied by this supplier
 * @author Marcelo Portilho
 */
public class MemoizedSupplier<T> implements Supplier<T> {

    private Supplier<T> delegate;
    private volatile boolean initialized;
    private T cache;
    private volatile ReentrantLock reentrantLock;

    /**
     * Creates a new {@link MemoizedSupplier} instance that caches the result of the delegate supplier.
     *
     * @param delegate the delegate supplier
     */
    public MemoizedSupplier(Supplier<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "Supplier delegate must be provided");
    }

    @Override
    public T get() {
        if (!initialized) {
            ReentrantLock lock = getReentrantLock();
            lock.lock();
            try {
                if (!initialized) {
                    T value = delegate.get();
                    cache = value;
                    initialized = true;
                    delegate = null; // to GC
                    return value;
                }
            } finally {
                lock.unlock();
                this.reentrantLock = null; // to GC
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
