package com.runestone.expeval.internal.navigation;

import org.jspecify.annotations.Nullable;

/**
 * Encapsulates the current element being tested inside a {@code [?(...)] } filter predicate.
 *
 * <p>A pool of {@code FilterContext} instances (one per active nesting level) is maintained by
 * {@code AbstractObjectEvaluator.FilterContextStack} to support nested filters such as
 * {@code [?(@.authors[?(@.name =~ ".*")])]}. Each instance is mutated in-place rather than
 * replaced, eliminating per-element allocation on hot filter paths.
 *
 * <p>Two bind methods cover the two filter modes:
 * <ul>
 *   <li>{@link #bindElement} — for collection filters; only {@code element} is set.</li>
 *   <li>{@link #bindMapEntry} — for map filters; {@code mapKey} and {@code mapValue} are set,
 *       {@code element} is {@code null}.</li>
 * </ul>
 */
public final class FilterContext {
    private @Nullable Object element;
    private @Nullable Object mapKey;
    private @Nullable Object mapValue;

    public FilterContext() {}

    /** Binds this context to a collection element; clears map-entry fields. */
    public void bindElement(Object element) {
        this.element = element;
        this.mapKey = null;
        this.mapValue = null;
    }

    /** Binds this context to a map entry; clears the element field. */
    public void bindMapEntry(Object key, Object value) {
        this.element = null;
        this.mapKey = key;
        this.mapValue = value;
    }

    /** The collection element, or {@code null} when this is a map-entry context. */
    public @Nullable Object element() {
        return element;
    }

    /** The map key, or {@code null} when this is a collection-element context. */
    public @Nullable Object mapKey() {
        return mapKey;
    }

    /** The map value, or {@code null} when this is a collection-element context. */
    public @Nullable Object mapValue() {
        return mapValue;
    }

    /** Returns {@code true} when this context represents a map-entry filter (not a collection-element filter). */
    public boolean isMapContext() {
        return mapKey != null;
    }
}
