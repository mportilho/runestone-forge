package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.memory.DefaultCalculationMemory;

import java.util.List;

/**
 * Immutable memory for one successful computation.
 *
 * <p>Persistence adapters should iterate from zero to {@link #variableCount()} or
 * {@link #calculationCount()} and read each key and value through the corresponding {@code *At} methods.
 * This indexed path creates no entry, list view, or iterator. {@link #variables()} and
 * {@link #calculations()} are convenience projections; requesting a non-empty list creates a small view
 * and accessing its elements creates transient entry records.
 *
 * <p>Values are the canonical references that participated in execution. Registered mutable Java objects
 * are not historical snapshots. Serialization, detached copies, transactions, persistence resources, and
 * their failure handling belong to the consuming adapter.
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
