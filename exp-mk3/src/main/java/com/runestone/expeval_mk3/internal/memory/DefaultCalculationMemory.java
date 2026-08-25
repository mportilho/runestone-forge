package com.runestone.expeval_mk3.internal.memory;

import com.runestone.expeval_mk3.api.CalculationEntry;
import com.runestone.expeval_mk3.api.CalculationKey;
import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.api.VariableEntry;
import com.runestone.expeval_mk3.api.VariableKey;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

public final class DefaultCalculationMemory implements CalculationMemory {

    private static final Object[] NO_VALUES = new Object[0];
    private static final DefaultCalculationMemory EMPTY = new DefaultCalculationMemory(
            List.of(), NO_VALUES, List.of(), NO_VALUES, null);

    private final List<VariableKey> variableKeys;
    private final Object[] variableValues;
    private final List<CalculationKey> calculationKeys;
    private final Object[] calculationValues;
    private final int[] calculationOrdinals;

    DefaultCalculationMemory(
            List<VariableKey> variableKeys,
            Object[] variableValues,
            List<CalculationKey> calculationKeys,
            Object[] calculationValues,
            int[] calculationOrdinals) {
        this.variableKeys = List.copyOf(variableKeys);
        this.variableValues = Objects.requireNonNull(variableValues, "variableValues");
        this.calculationKeys = List.copyOf(calculationKeys);
        this.calculationValues = Objects.requireNonNull(calculationValues, "calculationValues");
        this.calculationOrdinals = calculationOrdinals;
        if (this.variableKeys.size() != variableValues.length) {
            throw new IllegalArgumentException("variable keys and values must have the same length");
        }
        for (int index = 0; index < variableValues.length; index++) {
            Objects.requireNonNull(variableValues[index], "variableValues[" + index + "]");
        }
        if (calculationOrdinals == null && calculationValues.length > this.calculationKeys.size()) {
            throw new IllegalArgumentException("calculation values must fit the calculation schema");
        }
        if (calculationOrdinals != null && calculationOrdinals.length != calculationValues.length) {
            throw new IllegalArgumentException("calculation ordinals and values must have the same length");
        }
        if (calculationOrdinals != null) {
            int previous = -1;
            for (int ordinal : calculationOrdinals) {
                if (ordinal <= previous || ordinal >= this.calculationKeys.size()) {
                    throw new IllegalArgumentException("calculation ordinals must be strictly increasing and within the schema");
                }
                previous = ordinal;
            }
        }
    }

    public static CalculationMemory empty() {
        return EMPTY;
    }

    static DefaultCalculationMemory emptyInstance() {
        return EMPTY;
    }

    static DefaultCalculationMemory variables(List<VariableKey> variableKeys, Object[] variableValues) {
        return new DefaultCalculationMemory(variableKeys, variableValues, List.of(), NO_VALUES, null);
    }

    @Override
    public int variableCount() {
        return variableValues.length;
    }

    @Override
    public VariableKey variableKeyAt(int index) {
        return variableKeys.get(index);
    }

    @Override
    public Object variableValueAt(int index) {
        return variableValues[index];
    }

    @Override
    public List<VariableEntry> variables() {
        if (variableValues.length == 0) {
            return List.of();
        }
        return new AbstractList<>() {
            @Override
            public VariableEntry get(int index) {
                return new VariableEntry(variableKeyAt(index), variableValueAt(index));
            }

            @Override
            public int size() {
                return variableCount();
            }
        };
    }

    @Override
    public int calculationCount() {
        return calculationValues.length;
    }

    @Override
    public CalculationKey calculationKeyAt(int index) {
        int ordinal = calculationOrdinals == null ? index : calculationOrdinals[index];
        return calculationKeys.get(ordinal);
    }

    @Override
    public Object calculationValueAt(int index) {
        return calculationValues[index];
    }

    @Override
    public List<CalculationEntry> calculations() {
        if (calculationValues.length == 0) {
            return List.of();
        }
        return new AbstractList<>() {
            @Override
            public CalculationEntry get(int index) {
                return new CalculationEntry(calculationKeyAt(index), calculationValueAt(index));
            }

            @Override
            public int size() {
                return calculationCount();
            }
        };
    }
}
