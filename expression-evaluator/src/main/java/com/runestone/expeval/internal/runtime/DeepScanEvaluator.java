package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.navigation.TypeIntrospectionSupport;

import java.lang.invoke.MethodHandle;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class DeepScanEvaluator {

    private DeepScanEvaluator() {}

    static List<Object> evaluate(Object root, String propertyName) {
        DeepScanContext context = DeepScanContext.INSTANCE.get();
        List<Object> results = context.results;
        Set<Object> visited = context.visited;
        Deque<Object> queue = context.queue;

        results.clear();
        visited.clear();
        queue.clear();

        queue.add(root);
        while (!queue.isEmpty()) {
            Object node = queue.poll();
            if (node == null || !visited.add(node)) {
                continue;
            }
            if (propertyName == null) {
                scanAllValues(node, results, queue);
            } else {
                scanNamedProperty(node, propertyName, results, queue);
            }
        }
        return results;
    }

    private static void scanAllValues(Object node, List<Object> results, Deque<Object> queue) {
        if (node instanceof List<?> list) {
            queue.addAll(list);
        } else if (node instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                results.add(value);
                enqueueNested(value, queue);
            }
        } else {
            results.add(node);
        }
    }

    private static void scanNamedProperty(
            Object node,
            String propertyName,
            List<Object> results,
            Deque<Object> queue) {
        if (node instanceof List<?> list) {
            queue.addAll(list);
        } else if (node instanceof Map<?, ?> map) {
            scanMapProperty(map, propertyName, results, queue);
        } else {
            scanObjectProperty(node, propertyName, results);
        }
    }

    private static void scanMapProperty(
            Map<?, ?> map,
            String propertyName,
            List<Object> results,
            Deque<Object> queue) {
        @SuppressWarnings("unchecked")
        Object value = ((Map<String, Object>) map).get(propertyName);
        if (value != null) {
            results.add(value);
        }
        for (Object nestedValue : map.values()) {
            enqueueNested(nestedValue, queue);
        }
    }

    private static void scanObjectProperty(Object node, String propertyName, List<Object> results) {
        MethodHandle handle = TypeIntrospectionSupport.cachedProperty(node.getClass(), propertyName);
        if (handle == null) {
            return;
        }
        try {
            Object value = handle.invoke(node);
            if (value != null) {
                results.add(value);
            }
        } catch (Throwable ignored) {
            // Best-effort deep scan skips inaccessible properties.
        }
    }

    private static void enqueueNested(Object value, Deque<Object> queue) {
        if (value instanceof List<?> || value instanceof Map<?, ?>) {
            queue.add(value);
        }
    }
}
