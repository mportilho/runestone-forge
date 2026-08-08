package com.runestone.expeval_mk3.internal.plan;

import java.util.Map;

/**
 * Strict lookups into {@code SemanticModel} maps, shared by {@link ExecutionPlanBuilder} and
 * {@link CommonSubexpressionAnalyzer}: a missing entry means the semantic model is internally
 * inconsistent, so it is reported as such rather than surfacing as a {@code NullPointerException}.
 */
final class BindingLookup {

    private BindingLookup() {
    }

    static <K, V> V required(Map<K, V> values, K key, String description) {
        V value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("semantic model is missing " + description + " for " + key);
        }
        return value;
    }
}
