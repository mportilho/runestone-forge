package com.runestone.expeval_mk3.internal.memory;

import com.runestone.expeval_mk3.api.CalculationKey;
import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;

import java.util.List;
import java.util.Objects;

/** Standalone metadata used to capture and freeze one calculation-memory view. */
public final class CalculationMemorySchema {

    private final VariableMemorySchema variableSchema;
    private final List<CalculationKey> calculationKeys;

    public CalculationMemorySchema(VariableMemorySchema variableSchema, List<CalculationKey> calculationKeys) {
        this.variableSchema = Objects.requireNonNull(variableSchema, "variableSchema");
        this.calculationKeys = List.copyOf(Objects.requireNonNull(calculationKeys, "calculationKeys"));
    }

    public CalculationRecorder newRecorder() {
        return calculationKeys.isEmpty() ? null : new CalculationRecorder(calculationKeys.size());
    }

    public CalculationMemory freeze(ExecutionScope scope, CalculationRecorder recorder) {
        Objects.requireNonNull(scope, "scope");
        Object[] variableValues = variableSchema.copyValues(scope);
        if (recorder == null || recorder.count() == 0) {
            return variableValues.length == 0
                    ? DefaultCalculationMemory.emptyInstance()
                    : DefaultCalculationMemory.variables(variableSchema.keys(), variableValues);
        }
        return DefaultCalculationMemory.combined(
                variableSchema.keys(), variableValues, calculationKeys,
                recorder.exactValues(), recorder.exactOrdinals());
    }
}
