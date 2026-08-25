package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.memory.DefaultCalculationMemory;

import java.util.List;

/**
 * Immutable memory for one successful computation. Indexed access is the allocation-free persistence
 * path. Values are canonical execution references; registered Java objects are not historical snapshots.
 */
public interface CalculationMemory {

    static CalculationMemory empty() {
        return DefaultCalculationMemory.empty();
    }

    int variableCount();

    VariableKey variableKeyAt(int index);

    Object variableValueAt(int index);

    List<VariableEntry> variables();

    int calculationCount();

    CalculationKey calculationKeyAt(int index);

    Object calculationValueAt(int index);

    List<CalculationEntry> calculations();
}
