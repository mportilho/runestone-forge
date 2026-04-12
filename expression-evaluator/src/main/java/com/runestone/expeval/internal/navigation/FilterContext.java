package com.runestone.expeval.internal.navigation;

import org.jspecify.annotations.Nullable;

/**
 * Encapsulates the current element being tested inside a {@code [?(...)] } filter predicate.
 *
 * <p>A stack of {@code FilterContext} objects (one per active filter level) is maintained by
 * {@code AbstractObjectEvaluator} to support nested filters such as
 * {@code [?(@.authors[?(@.name =~ ".*")])]}. The stack is pushed before each element is tested
 * and popped in a {@code finally} block to guarantee cleanup even on exception.
 *
 * <p>Two factory methods cover the two filter modes:
 * <ul>
 *   <li>{@link #ofElement} — for collection filters; only {@code element} is set.</li>
 *   <li>{@link #ofMapEntry} — for map filters; {@code mapKey} and {@code mapValue} are set,
 *       {@code element} is {@code null}.</li>
 * </ul>
 */
public record FilterContext(
        @Nullable Object element,
        @Nullable Object mapKey,
        @Nullable Object mapValue
) {

    /** Creates a context for a collection filter; {@code mapKey} and {@code mapValue} are {@code null}. */
    public static FilterContext ofElement(Object element) {
        return new FilterContext(element, null, null);
    }

    /** Creates a context for a map filter; {@code element} is {@code null}. */
    public static FilterContext ofMapEntry(Object key, Object value) {
        return new FilterContext(null, key, value);
    }

    /** Returns {@code true} when this context represents a map-entry filter (not a collection-element filter). */
    public boolean isMapContext() {
        return mapKey != null;
    }
}
