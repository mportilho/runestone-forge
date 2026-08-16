package com.runestone.expeval_mk3.internal.cache;

/**
 * The monotonic time source behind {@link CompilationCache}'s optional access-based expiration,
 * deliberately distinct from the semantic {@link java.time.Clock} an {@code ExpressionEngine} uses for
 * current temporal values: advancing business time must never change eviction behavior. The default
 * ticker delegates to {@link System#nanoTime()}; tests inject a fake, controllable ticker instead of
 * sleeping real time.
 */
@FunctionalInterface
public interface MonotonicTicker {

    MonotonicTicker SYSTEM = System::nanoTime;

    long read();
}
