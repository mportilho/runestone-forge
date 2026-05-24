package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.api.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class CollectionAccessOps {

    private CollectionAccessOps() {
    }

    /** {@code [n]} -- single index access; supports negative indices. */
    static Object applyIndex(Object collection, int index, String source) {
        List<Object> list = requireList(collection, "index", source);
        int effective = index < 0 ? list.size() + index : index;
        if (effective < 0 || effective >= list.size()) {
            throw new ExpressionEvaluationException(source, "INDEX_OUT_OF_BOUNDS",
                    "index " + index + " is out of bounds for collection of size " + list.size(), null);
        }
        return list.get(effective);
    }

    /** {@code ["key"]} -- key lookup in a {@link Map}. */
    @SuppressWarnings("unchecked")
    static Object applyMapKey(Object map, String key, String source) {
        if (!(map instanceof Map<?, ?> currentMap)) {
            throw new ExpressionEvaluationException(source, "MAP_KEY_TYPE_MISMATCH",
                    "map-key access requires a Map but got: "
                    + (map == null ? "null" : map.getClass().getSimpleName()), null);
        }
        return ((Map<String, Object>) currentMap).get(key);
    }

    /** {@code [start:end]} -- Python-style slice. */
    static List<Object> applySlice(Object collection, Integer start, Integer end, String source) {
        List<Object> list = requireList(collection, "slice", source);
        int size = list.size();
        int from = start == null ? 0 : (start < 0 ? Math.max(0, size + start) : Math.min(start, size));
        int to = end == null ? size : (end < 0 ? Math.max(0, size + end) : Math.min(end, size));
        if (from >= to) {
            return List.of();
        }
        return List.copyOf(list.subList(from, to));
    }

    /** {@code [*]} or {@code .*} -- all elements or all map values. */
    @SuppressWarnings("unchecked")
    static List<Object> applyWildcard(Object current) {
        if (current instanceof List<?> list) {
            return (List<Object>) list;
        }
        if (current instanceof Map<?, ?> map) {
            return new ArrayList<>((Collection<Object>) map.values());
        }
        return List.of(current);
    }

    /** {@code ..keys()} or {@code ..values()} -- map projection. */
    @SuppressWarnings("unchecked")
    static List<Object> applyMapProjection(Object current, MapProjectionKind kind, String source) {
        if (current instanceof Map<?, ?> map) {
            Map<String, Object> typed = (Map<String, Object>) map;
            return kind == MapProjectionKind.KEYS
                    ? new ArrayList<>(typed.keySet())
                    : new ArrayList<>(typed.values());
        }
        // A map-entry filter (e.g. map[?(@.key.x > v)]) already returns a List of values.
        // Applying ..values() on that result is a no-op: the list IS the values collection.
        if (kind == MapProjectionKind.VALUES && current instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new ExpressionEvaluationException(source, "MAP_PROJECTION_TYPE_MISMATCH",
                "map projection requires a Map but got: "
                + (current == null ? "null" : current.getClass().getSimpleName()), null);
    }

    @SuppressWarnings("unchecked")
    static List<Object> requireList(Object value, String operation, String source) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new ExpressionEvaluationException(source, "TYPE_MISMATCH",
                operation + " requires a List but got: "
                + (value == null ? "null" : value.getClass().getName()), null);
    }
}
