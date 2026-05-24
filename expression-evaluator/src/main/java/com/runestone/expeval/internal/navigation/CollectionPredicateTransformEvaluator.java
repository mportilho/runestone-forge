package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.internal.execution.eval.*;
import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.runtime.RuntimeServices;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CollectionPredicateTransformEvaluator {

    private CollectionPredicateTransformEvaluator() {}

    static Object filter(
            Object current,
            ExecutableNode predicate,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            NodeEvaluator nodeEvaluator) {
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        if (current instanceof Map<?, ?> map) {
            return filterMap(map, predicate, scope, source, runtimeServices, nodeEvaluator, stack);
        }
        List<Object> list = CollectionAccessOps.requireList(current, "filter", source);
        return filterList(list, predicate, scope, source, runtimeServices, nodeEvaluator, stack);
    }

    static List<Object> map(
            Object current,
            ExecutableNode transform,
            ExecutionScope scope,
            String source,
            NodeEvaluator nodeEvaluator) {
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        if (current instanceof Map<?, ?> map) {
            return mapEntries(map, transform, scope, nodeEvaluator, stack);
        }
        List<?> list = CollectionAccessOps.requireList(current, "map", source);
        return mapElements(list, transform, scope, nodeEvaluator, stack);
    }

    private static Map<Object, Object> filterMap(
            Map<?, ?> map,
            ExecutableNode predicate,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            NodeEvaluator nodeEvaluator,
            FilterContextStack stack) {
        Map<Object, Object> result = new LinkedHashMap<>(map.size() * 2);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            stack.pushMapEntry(entry.getKey(), entry.getValue());
            try {
                if (asBoolean(nodeEvaluator.evaluate(predicate, scope), source, runtimeServices)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    private static List<Object> filterList(
            List<Object> list,
            ExecutableNode predicate,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            NodeEvaluator nodeEvaluator,
            FilterContextStack stack) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            stack.pushElement(element);
            try {
                if (asBoolean(nodeEvaluator.evaluate(predicate, scope), source, runtimeServices)) {
                    result.add(element);
                }
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    private static List<Object> mapEntries(
            Map<?, ?> map,
            ExecutableNode transform,
            ExecutionScope scope,
            NodeEvaluator nodeEvaluator,
            FilterContextStack stack) {
        List<Object> result = new ArrayList<>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            stack.pushMapEntry(entry.getKey(), entry.getValue());
            try {
                result.add(nodeEvaluator.evaluate(transform, scope));
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    private static List<Object> mapElements(
            List<?> list,
            ExecutableNode transform,
            ExecutionScope scope,
            NodeEvaluator nodeEvaluator,
            FilterContextStack stack) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            stack.pushElement(element);
            try {
                result.add(nodeEvaluator.evaluate(transform, scope));
            } finally {
                stack.pop();
            }
        }
        return result;
    }

    private static boolean asBoolean(Object value, String source, RuntimeServices runtimeServices) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        try {
            return runtimeServices.asBoolean(value);
        } catch (IllegalStateException e) {
            throw new ExpressionEvaluationException(source, "NULL_VALUE",
                    "cannot use null value as a boolean", null);
        }
    }
}
