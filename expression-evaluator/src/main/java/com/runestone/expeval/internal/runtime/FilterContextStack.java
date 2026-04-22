package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.navigation.FilterContext;

import java.util.Arrays;

/**
 * Per-thread pool of reusable {@link FilterContext} frames to support nested filter predicates
 * such as {@code list[?(@.items[?(@.active)])]}. Frames are mutated in-place rather than
 * replaced per element, eliminating per-element allocation on hot filter paths.
 * Thread-local because compiled expression objects are shared and may be invoked concurrently.
 */
final class FilterContextStack {

    static final ThreadLocal<FilterContextStack> INSTANCE =
            ThreadLocal.withInitial(FilterContextStack::new);

    private FilterContext[] frames;
    private int depth;

    private FilterContextStack() {
        frames = new FilterContext[4];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new FilterContext();
        }
    }

    void pushElement(Object element) {
        ensureCapacity();
        frames[depth++].bindElement(element);
    }

    void pushMapEntry(Object key, Object value) {
        ensureCapacity();
        frames[depth++].bindMapEntry(key, value);
    }

    void pop() {
        depth--;
    }

    FilterContext peek() {
        return depth > 0 ? frames[depth - 1] : null;
    }

    private void ensureCapacity() {
        if (depth == frames.length) {
            int newLen = frames.length * 2;
            frames = Arrays.copyOf(frames, newLen);
            for (int i = depth; i < newLen; i++) {
                frames[i] = new FilterContext();
            }
        }
    }
}
