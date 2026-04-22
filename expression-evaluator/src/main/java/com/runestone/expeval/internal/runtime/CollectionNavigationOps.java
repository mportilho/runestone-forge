package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.navigation.MapProjectionKind;
import com.runestone.expeval.internal.navigation.TypeIntrospectionSupport;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

final class CollectionNavigationOps {

    private CollectionNavigationOps() {}

    /** {@code [n]} — single index access; supports negative indices. */
    static Object applyIndex(Object collection, int index, String source) {
        List<Object> list = requireList(collection, "index", source);
        int effective = index < 0 ? list.size() + index : index;
        if (effective < 0 || effective >= list.size()) {
            throw new ExpressionEvaluationException(source, "INDEX_OUT_OF_BOUNDS",
                    "index " + index + " is out of bounds for collection of size " + list.size(), null);
        }
        return list.get(effective);
    }

    /** {@code ["key"]} — key lookup in a {@link Map}. */
    @SuppressWarnings("unchecked")
    static Object applyMapKey(Object map, String key, String source) {
        if (!(map instanceof Map<?, ?> m)) {
            throw new ExpressionEvaluationException(source, "MAP_KEY_TYPE_MISMATCH",
                    "map-key access requires a Map but got: "
                    + (map == null ? "null" : map.getClass().getSimpleName()), null);
        }
        return ((Map<String, Object>) m).get(key);
    }

    /** {@code [start:end]} — Python-style slice. */
    static List<Object> applySlice(Object collection, Integer start, Integer end, String source) {
        List<Object> list = requireList(collection, "slice", source);
        int size = list.size();
        int from = start == null ? 0 : (start < 0 ? Math.max(0, size + start) : Math.min(start, size));
        int to   = end   == null ? size : (end < 0 ? Math.max(0, size + end)   : Math.min(end,   size));
        if (from >= to) return List.of();
        return List.copyOf(list.subList(from, to));
    }

    /** {@code [*]} or {@code .*} — all elements or all map values. */
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

    /** {@code [?(<predicate>)]} — element-wise filter on a list or map. */
    static Object applyFilter(Object current, ExecutableNode predicate, ExecutionScope scope,
            String source, RuntimeServices runtimeServices, NodeEvaluator eval) {
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        if (current instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>(map.size() * 2);
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                stack.pushMapEntry(key, value);
                try {
                    if (asBoolean(eval.evaluate(predicate, scope), source, runtimeServices)) {
                        result.put(key, value);
                    }
                } finally {
                    stack.pop();
                }
            }
            return result;
        }
        List<Object> list = requireList(current, "filter", source);
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            stack.pushElement(element);
            try {
                if (asBoolean(eval.evaluate(predicate, scope), source, runtimeServices)) {
                    result.add(element);
                }
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    /**
     * {@code ..name} or {@code ..*} — BFS recursive deep scan.
     * Collects the named property (or all values for wildcard) from every reachable node.
     * Reuses thread-local structures to eliminate per-invocation allocations.
     */
    static List<Object> applyDeepScan(Object root, String propertyName, String source) {
        DeepScanContext ctx = DeepScanContext.INSTANCE.get();
        List<Object> results = ctx.results;
        Set<Object> visited = ctx.visited;
        Deque<Object> queue = ctx.queue;

        results.clear();
        visited.clear();
        queue.clear();

        queue.add(root);
        while (!queue.isEmpty()) {
            Object node = queue.poll();
            if (node == null || !visited.add(node)) continue;
            if (propertyName == null) {
                if (node instanceof List<?> list) {
                    queue.addAll(list);
                } else if (node instanceof Map<?, ?> map) {
                    for (Object v : map.values()) {
                        results.add(v);
                        if (v instanceof List<?> || v instanceof Map<?, ?>) queue.add(v);
                    }
                } else {
                    results.add(node);
                }
            } else {
                if (node instanceof List<?> list) {
                    queue.addAll(list);
                } else if (node instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked")
                    Object val = ((Map<String, Object>) map).get(propertyName);
                    if (val != null) results.add(val);
                    for (Object v : map.values()) {
                        if (v instanceof List<?> || v instanceof Map<?, ?>) queue.add(v);
                    }
                } else {
                    MethodHandle handle = TypeIntrospectionSupport.cachedProperty(node.getClass(), propertyName);
                    if (handle != null) {
                        try {
                            Object val = handle.invoke(node);
                            if (val != null) results.add(val);
                        } catch (Throwable ignored) {
                            // best-effort deep scan — skip inaccessible properties
                        }
                    }
                }
            }
        }
        return results;
    }

    /** {@code ..sum()}, {@code ..avg()}, {@code ..prod()}, etc. — numeric aggregations over a list. */
    static Object applyAggregation(Object current, VectorAggregationKind kind,
            @Nullable ExecutableNode transform, ExecutionScope scope,
            String source, RuntimeServices runtimeServices, MathContext mathContext, NodeEvaluator eval) {
        if (current instanceof Map<?, ?> m && kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(m.size());
        }
        List<?> list = requireList(current, "aggregation", source);
        if (kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(list.size());
        }
        if (list.isEmpty()) {
            return switch (kind) {
                case SUM -> BigDecimal.ZERO;
                case PROD -> BigDecimal.ONE;
                default -> null;
            };
        }
        List<BigDecimal> values = toNumericList(list, transform, scope, source, runtimeServices, eval);
        BigDecimal acc = values.getFirst();
        switch (kind) {
            case SUM -> {
                for (int i = 1; i < values.size(); i++) acc = acc.add(values.get(i), mathContext);
                return acc;
            }
            case AVG -> {
                for (int i = 1; i < values.size(); i++) acc = acc.add(values.get(i), mathContext);
                return acc.divide(BigDecimal.valueOf(values.size()), mathContext);
            }
            case MIN -> {
                for (int i = 1; i < values.size(); i++) {
                    BigDecimal v = values.get(i);
                    if (v.compareTo(acc) < 0) acc = v;
                }
                return acc;
            }
            case MAX -> {
                for (int i = 1; i < values.size(); i++) {
                    BigDecimal v = values.get(i);
                    if (v.compareTo(acc) > 0) acc = v;
                }
                return acc;
            }
            case PROD -> {
                for (int i = 1; i < values.size(); i++) acc = acc.multiply(values.get(i), mathContext);
                return acc;
            }
            default -> throw new IllegalStateException("Unhandled aggregation kind: " + kind);
        }
    }

    /** {@code ..map(@ -> expr)} — transforms each element (or map entry) into a new list. */
    @SuppressWarnings("unchecked")
    static List<Object> applyMapTransform(Object current, ExecutableNode transform,
            ExecutionScope scope, String source, NodeEvaluator eval) {
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        if (current instanceof Map<?, ?> map) {
            List<Object> result = new ArrayList<>(map.size());
            for (Map.Entry<?, ?> entry : ((Map<Object, Object>) map).entrySet()) {
                stack.pushMapEntry(entry.getKey(), entry.getValue());
                try {
                    result.add(eval.evaluate(transform, scope));
                } finally {
                    stack.pop();
                }
            }
            return result;
        }
        List<?> list = requireList(current, "map", source);
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            stack.pushElement(element);
            try {
                result.add(eval.evaluate(transform, scope));
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    /** {@code ..keys()} or {@code ..values()} — map projection. */
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

    /**
     * {@code ..funcName(args)} — invoke a catalog function with the current
     * collection/map as the implicit first argument.
     */
    static Object applyCollectionFunction(Object current, ExecutablePropertyChain.ExecutableCollectionFunction cf,
            ExecutionScope scope, String source, RuntimeServices runtimeServices, NodeEvaluator eval) {
        ResolvedFunctionBinding binding = cf.binding();
        if (binding == null || binding.descriptor() == null) {
            throw new ExpressionEvaluationException(source, "UNRESOLVED_COLLECTION_FUNCTION",
                    "collection function could not be resolved", null);
        }
        FunctionDescriptor descriptor = binding.descriptor();
        List<ExecutableNode> extraArgNodes = cf.arguments();
        int totalArity = 1 + extraArgNodes.size();
        List<Class<?>> paramTypes = descriptor.parameterTypes();

        Object[] args = new Object[totalArity];
        args[0] = runtimeServices.coerce(current, paramTypes.getFirst());
        for (int i = 0; i < extraArgNodes.size(); i++) {
            Object evaluated = eval.evaluate(extraArgNodes.get(i), scope);
            args[i + 1] = runtimeServices.coerce(evaluated, paramTypes.get(i + 1));
        }
        Object result = descriptor.invoke(args);
        return runtimeServices.coerceToResolvedType(result, binding.returnType());
    }

    @SuppressWarnings("unchecked")
    static List<Object> requireList(Object value, String operation, String source) {
        if (value instanceof List<?> list) return (List<Object>) list;
        throw new ExpressionEvaluationException(source, "TYPE_MISMATCH",
                operation + " requires a List but got: "
                + (value == null ? "null" : value.getClass().getName()), null);
    }

    private static List<BigDecimal> toNumericList(List<?> list, @Nullable ExecutableNode transform,
            ExecutionScope scope, String source, RuntimeServices runtimeServices, NodeEvaluator eval) {
        if (transform == null) {
            List<BigDecimal> result = new ArrayList<>(list.size());
            for (Object element : list) result.add(asBigDecimal(element, source, runtimeServices));
            return result;
        }
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        List<BigDecimal> result = new ArrayList<>(list.size());
        for (Object element : list) {
            stack.pushElement(element);
            try {
                result.add(asBigDecimal(eval.evaluate(transform, scope), source, runtimeServices));
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    private static BigDecimal asBigDecimal(Object value, String source, RuntimeServices runtimeServices) {
        if (value instanceof BigDecimal bd) return bd;
        try {
            return runtimeServices.asNumber(value);
        } catch (IllegalStateException e) {
            throw new ExpressionEvaluationException(source, "NULL_VALUE",
                    "cannot use null value as a number", null);
        }
    }

    private static boolean asBoolean(Object value, String source, RuntimeServices runtimeServices) {
        if (value instanceof Boolean b) return b;
        try {
            return runtimeServices.asBoolean(value);
        } catch (IllegalStateException e) {
            throw new ExpressionEvaluationException(source, "NULL_VALUE",
                    "cannot use null value as a boolean", null);
        }
    }
}
