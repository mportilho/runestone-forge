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
    private final ReentrantLock lock = new ReentrantLock();

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
            }
        }
        return cache;
    }

}