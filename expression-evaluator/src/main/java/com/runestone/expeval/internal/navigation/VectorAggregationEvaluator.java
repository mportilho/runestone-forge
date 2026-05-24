package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.internal.execution.eval.*;
import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;

final class VectorAggregationEvaluator {

    private VectorAggregationEvaluator() {}

    static Object evaluate(
            Object current,
            VectorAggregationKind kind,
            @Nullable ExecutableNode transform,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            MathContext mathContext,
            NodeEvaluator nodeEvaluator) {
        if (current instanceof Map<?, ?> map && kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(map.size());
        }
        List<?> list = CollectionAccessOps.requireList(current, "aggregation", source);
        if (kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(list.size());
        }
        if (list.isEmpty()) {
            return emptyAggregationValue(kind);
        }
        if (transform == null) {
            return aggregatePlain(list, kind, source, runtimeServices, mathContext);
        }
        return aggregateTransformed(list, kind, transform, scope, source, runtimeServices, mathContext, nodeEvaluator);
    }

    private static Object emptyAggregationValue(VectorAggregationKind kind) {
        return switch (kind) {
            case SUM -> BigDecimal.ZERO;
            case PROD -> BigDecimal.ONE;
            default -> null;
        };
    }

    private static Object aggregatePlain(
            List<?> values,
            VectorAggregationKind kind,
            String source,
            RuntimeServices runtimeServices,
            MathContext mathContext) {
        switch (kind) {
            case SUM -> {
                BigDecimal accumulator = BigDecimal.ZERO;
                for (Object value : values) {
                    accumulator = accumulator.add(asBigDecimal(value, source, runtimeServices), mathContext);
                }
                return accumulator;
            }
            case AVG -> {
                BigDecimal accumulator = BigDecimal.ZERO;
                for (Object value : values) {
                    accumulator = accumulator.add(asBigDecimal(value, source, runtimeServices), mathContext);
                }
                return accumulator.divide(BigDecimal.valueOf(values.size()), mathContext);
            }
            case MIN -> {
                BigDecimal accumulator = asBigDecimal(values.getFirst(), source, runtimeServices);
                for (int index = 1; index < values.size(); index++) {
                    BigDecimal value = asBigDecimal(values.get(index), source, runtimeServices);
                    if (value.compareTo(accumulator) < 0) {
                        accumulator = value;
                    }
                }
                return accumulator;
            }
            case MAX -> {
                BigDecimal accumulator = asBigDecimal(values.getFirst(), source, runtimeServices);
                for (int index = 1; index < values.size(); index++) {
                    BigDecimal value = asBigDecimal(values.get(index), source, runtimeServices);
                    if (value.compareTo(accumulator) > 0) {
                        accumulator = value;
                    }
                }
                return accumulator;
            }
            case PROD -> {
                BigDecimal accumulator = asBigDecimal(values.getFirst(), source, runtimeServices);
                for (int index = 1; index < values.size(); index++) {
                    accumulator = accumulator.multiply(asBigDecimal(values.get(index), source, runtimeServices), mathContext);
                }
                return accumulator;
            }
            default -> throw new IllegalStateException("Unhandled aggregation kind: " + kind);
        }
    }

    private static Object aggregateTransformed(
            List<?> list,
            VectorAggregationKind kind,
            ExecutableNode transform,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            MathContext mathContext,
            NodeEvaluator nodeEvaluator) {
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        switch (kind) {
            case SUM -> {
                BigDecimal accumulator = BigDecimal.ZERO;
                for (Object element : list) {
                    accumulator = accumulator.add(
                            evaluateTransformedNumber(element, transform, scope, source, runtimeServices, nodeEvaluator, stack),
                            mathContext);
                }
                return accumulator;
            }
            case AVG -> {
                BigDecimal accumulator = BigDecimal.ZERO;
                for (Object element : list) {
                    accumulator = accumulator.add(
                            evaluateTransformedNumber(element, transform, scope, source, runtimeServices, nodeEvaluator, stack),
                            mathContext);
                }
                return accumulator.divide(BigDecimal.valueOf(list.size()), mathContext);
            }
            case MIN -> {
                BigDecimal accumulator = evaluateTransformedNumber(
                        list.getFirst(), transform, scope, source, runtimeServices, nodeEvaluator, stack);
                for (int index = 1; index < list.size(); index++) {
                    BigDecimal value = evaluateTransformedNumber(
                            list.get(index), transform, scope, source, runtimeServices, nodeEvaluator, stack);
                    if (value.compareTo(accumulator) < 0) {
                        accumulator = value;
                    }
                }
                return accumulator;
            }
            case MAX -> {
                BigDecimal accumulator = evaluateTransformedNumber(
                        list.getFirst(), transform, scope, source, runtimeServices, nodeEvaluator, stack);
                for (int index = 1; index < list.size(); index++) {
                    BigDecimal value = evaluateTransformedNumber(
                            list.get(index), transform, scope, source, runtimeServices, nodeEvaluator, stack);
                    if (value.compareTo(accumulator) > 0) {
                        accumulator = value;
                    }
                }
                return accumulator;
            }
            case PROD -> {
                BigDecimal accumulator = evaluateTransformedNumber(
                        list.getFirst(), transform, scope, source, runtimeServices, nodeEvaluator, stack);
                for (int index = 1; index < list.size(); index++) {
                    accumulator = accumulator.multiply(
                            evaluateTransformedNumber(
                                    list.get(index), transform, scope, source, runtimeServices, nodeEvaluator, stack),
                            mathContext);
                }
                return accumulator;
            }
            default -> throw new IllegalStateException("Unhandled aggregation kind: " + kind);
        }
    }

    private static BigDecimal evaluateTransformedNumber(
            Object element,
            ExecutableNode transform,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            NodeEvaluator nodeEvaluator,
            FilterContextStack stack) {
        stack.pushElement(element);
        try {
            return asBigDecimal(nodeEvaluator.evaluate(transform, scope), source, runtimeServices);
        } finally {
            stack.pop();
        }
    }

    private static BigDecimal asBigDecimal(Object value, String source, RuntimeServices runtimeServices) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        try {
            return runtimeServices.asNumber(value);
        } catch (IllegalStateException e) {
            throw new ExpressionEvaluationException(source, "NULL_VALUE",
                    "cannot use null value as a number", null);
        }
    }
}
