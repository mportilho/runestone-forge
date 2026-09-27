package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.ExpressionResourceLimits;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Validates evaluator-owned value shapes without recursive Java calls or a global visited set. */
public final class ValueShapeValidator {
    private ValueShapeValidator() {
    }

    public static Violation check(Object value, ExpressionResourceLimits limits) {
        return check(value, limits, null, null);
    }

    public static Violation check(Object value, ExpressionResourceLimits limits,
                                  ExecutionScope scope, com.runestone.expeval_mk3.api.SourceSpan span) {
        Violation scalarViolation = checkScalar(value, limits);
        if (scalarViolation != null || !(value instanceof List<?> || value instanceof Map<?, ?>)) {
            return scalarViolation;
        }
        Traversal pending = new Traversal();
        IdentityHashMap<Object, Integer> deepestVisited = new IdentityHashMap<>();
        pending.push(value, 0);
        while (!pending.isEmpty()) {
            pending.pop();
            Object current = pending.currentValue;
            int depth = pending.currentDepth;
            Violation currentViolation = checkScalar(current, limits);
            if (currentViolation != null) {
                return currentViolation;
            }
            if (current instanceof List<?> list) {
                if (depth >= limits.maxValueDepth()) {
                    return new Violation(Kind.VALUE_DEPTH, "maxValueDepth", limits.maxValueDepth());
                }
                if (list.size() > limits.maxMaterializedSize()) {
                    return new Violation(Kind.MATERIALIZATION, "maxMaterializedSize", limits.maxMaterializedSize());
                }
                Integer previousDepth = deepestVisited.get(current);
                if (previousDepth != null && previousDepth >= depth) {
                    continue;
                }
                deepestVisited.put(current, depth);
                for (int index = list.size() - 1; index >= 0; index--) {
                    visit(scope, span);
                    Object child = list.get(index);
                    if (child == null) {
                        return new Violation(Kind.FORBIDDEN_NULL, "null container member", 0);
                    }
                    pending.push(child, depth + 1);
                }
            } else if (current instanceof Map<?, ?> map) {
                if (depth >= limits.maxValueDepth()) {
                    return new Violation(Kind.VALUE_DEPTH, "maxValueDepth", limits.maxValueDepth());
                }
                if (map.size() > limits.maxMaterializedSize()) {
                    return new Violation(Kind.MATERIALIZATION, "maxMaterializedSize", limits.maxMaterializedSize());
                }
                Integer previousDepth = deepestVisited.get(current);
                if (previousDepth != null && previousDepth >= depth) {
                    continue;
                }
                deepestVisited.put(current, depth);
                Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    visit(scope, span);
                    Map.Entry<?, ?> child = iterator.next();
                    if (child.getKey() == null || child.getValue() == null) {
                        return new Violation(Kind.FORBIDDEN_NULL, "null container member", 0);
                    }
                    pending.push(child.getKey(), depth + 1);
                    pending.push(child.getValue(), depth + 1);
                }
            }
        }
        return null;
    }

    private static void visit(ExecutionScope scope, com.runestone.expeval_mk3.api.SourceSpan span) {
        if (scope != null) {
            scope.visitTraversalStep(span);
        }
    }

    private static Violation checkScalar(Object value, ExpressionResourceLimits limits) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text && text.length() > limits.maxTextLength()) {
            return new Violation(Kind.TEXT_LENGTH, "maxTextLength", limits.maxTextLength());
        }
        if (value instanceof BigDecimal number) {
            if (number.precision() > limits.maxNumericPrecision()) {
                return new Violation(Kind.NUMERIC_PRECISION, "maxNumericPrecision", limits.maxNumericPrecision());
            }
            if (Math.abs((long) number.scale()) > limits.maxNumericScaleMagnitude()) {
                return new Violation(Kind.NUMERIC_SCALE, "maxNumericScaleMagnitude", limits.maxNumericScaleMagnitude());
            }
        }
        return null;
    }

    /** Saturating preflight for predictable text growth; no output string is allocated. */
    public static Violation repeatedText(String text, BigDecimal times, ExpressionResourceLimits limits) {
        long count;
        try {
            count = times.longValueExact();
        } catch (ArithmeticException invalidCount) {
            return null; // The built-in still owns the functional integer argument contract.
        }
        if (count < 0) {
            return null;
        }
        long length = count == 0 || text.isEmpty() ? 0
                : count > Long.MAX_VALUE / text.length() ? Long.MAX_VALUE : count * text.length();
        return length > limits.maxTextLength()
                ? new Violation(Kind.TEXT_LENGTH, "maxTextLength", limits.maxTextLength()) : null;
    }

    private static final class Traversal {
        private Object[] values = new Object[16];
        private int[] depths = new int[16];
        private int size;
        private Object currentValue;
        private int currentDepth;

        private void push(Object value, int depth) {
            if (size == values.length) {
                values = Arrays.copyOf(values, size * 2);
                depths = Arrays.copyOf(depths, size * 2);
            }
            values[size] = value;
            depths[size++] = depth;
        }

        private void pop() {
            int index = --size;
            currentValue = values[index];
            currentDepth = depths[index];
            values[index] = null;
        }

        private boolean isEmpty() {
            return size == 0;
        }
    }

    public enum Kind {
        TEXT_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, VALUE_DEPTH, MATERIALIZATION, FORBIDDEN_NULL
    }

    public record Violation(Kind kind, String property, int maximum) {
        public String message() {
            return property + " " + maximum + " exceeded";
        }
    }
}
