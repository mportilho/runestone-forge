package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.internal.execution.eval.*;
import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
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
        List<BigDecimal> values = toNumericList(list, transform, scope, source, runtimeServices, nodeEvaluator);
        return aggregate(values, kind, mathContext);
    }

    private static Object emptyAggregationValue(VectorAggregationKind kind) {
        return switch (kind) {
            case SUM -> BigDecimal.ZERO;
            case PROD -> BigDecimal.ONE;
            default -> null;
        };
    }

    private static Object aggregate(List<BigDecimal> values, VectorAggregationKind kind, MathContext mathContext) {
        BigDecimal accumulator = values.getFirst();
        switch (kind) {
            case SUM -> {
                for (int index = 1; index < values.size(); index++) {
                    accumulator = accumulator.add(values.get(index), mathContext);
                }
                return accumulator;
            }
            case AVG -> {
                for (int index = 1; index < values.size(); index++) {
                    accumulator = accumulator.add(values.get(index), mathContext);
                }
                return accumulator.divide(BigDecimal.valueOf(values.size()), mathContext);
            }
            case MIN -> {
                for (int index = 1; index < values.size(); index++) {
                    BigDecimal value = values.get(index);
                    if (value.compareTo(accumulator) < 0) {
                        accumulator = value;
                    }
                }
                return accumulator;
            }
            case MAX -> {
                for (int index = 1; index < values.size(); index++) {
                    BigDecimal value = values.get(index);
                    if (value.compareTo(accumulator) > 0) {
                        accumulator = value;
                    }
                }
                return accumulator;
            }
            case PROD -> {
                for (int index = 1; index < values.size(); index++) {
                    accumulator = accumulator.multiply(values.get(index), mathContext);
                }
                return accumulator;
            }
            default -> throw new IllegalStateException("Unhandled aggregation kind: " + kind);
        }
    }

    private static List<BigDecimal> toNumericList(
            List<?> list,
            @Nullable ExecutableNode transform,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            NodeEvaluator nodeEvaluator) {
        if (transform == null) {
            List<BigDecimal> result = new ArrayList<>(list.size());
            for (Object element : list) {
                result.add(asBigDecimal(element, source, runtimeServices));
            }
            return result;
        }
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        List<BigDecimal> result = new ArrayList<>(list.size());
        for (Object element : list) {
            stack.pushElement(element);
            try {
                result.add(asBigDecimal(nodeEvaluator.evaluate(transform, scope), source, runtimeServices));
            } finally {
                stack.pop();
            }
        }
        return result;
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
