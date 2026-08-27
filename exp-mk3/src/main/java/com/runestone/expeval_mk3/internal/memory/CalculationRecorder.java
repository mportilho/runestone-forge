package com.runestone.expeval_mk3.internal.memory;

import java.util.Arrays;

/** Execution-local append-only storage for reached calculation points. */
public final class CalculationRecorder {

    private static final int MAX_INITIAL_CAPACITY = 8;

    private final int calculationPointCount;
    private Object[] values;
    private int[] ordinals;
    private int count;

    CalculationRecorder(int calculationPointCount) {
        if (calculationPointCount <= 0) {
            throw new IllegalArgumentException("calculationPointCount must be positive");
        }
        this.calculationPointCount = calculationPointCount;
        values = new Object[Math.min(MAX_INITIAL_CAPACITY, calculationPointCount)];
    }

    public void append(int ordinal, Object value) {
        if (ordinal < 0 || ordinal >= calculationPointCount) {
            throw new IllegalArgumentException("calculation ordinal is outside the schema: " + ordinal);
        }
        int previous = count == 0 ? -1 : ordinals == null ? count - 1 : ordinals[count - 1];
        if (ordinal <= previous) {
            throw new IllegalStateException("calculation ordinals must be appended in strictly increasing order");
        }
        if (count == values.length) {
            int capacity = Math.min(calculationPointCount, Math.max(1, values.length << 1));
            values = Arrays.copyOf(values, capacity);
            if (ordinals != null) {
                ordinals = Arrays.copyOf(ordinals, capacity);
            }
        }
        if (ordinals == null && ordinal != count) {
            ordinals = new int[values.length];
            for (int index = 0; index < count; index++) {
                ordinals[index] = index;
            }
        }
        values[count] = value;
        if (ordinals != null) {
            ordinals[count] = ordinal;
        }
        count++;
    }

    int count() {
        return count;
    }

    Object[] exactValues() {
        return values.length == count ? values : Arrays.copyOf(values, count);
    }

    int[] exactOrdinals() {
        return ordinals == null ? null : ordinals.length == count ? ordinals : Arrays.copyOf(ordinals, count);
    }
}
