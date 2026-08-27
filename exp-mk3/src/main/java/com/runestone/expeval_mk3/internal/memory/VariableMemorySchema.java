package com.runestone.expeval_mk3.internal.memory;

import com.runestone.expeval_mk3.api.VariableKey;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;

import java.util.List;
import java.util.Objects;

/** Standalone variable metadata that cannot retain the executable plan or an execution frame. */
public final class VariableMemorySchema {

    private final List<VariableKey> keys;
    private final int[] frameSlots;

    public VariableMemorySchema(VariableKey[] keys, int[] frameSlots) {
        Objects.requireNonNull(keys, "keys");
        Objects.requireNonNull(frameSlots, "frameSlots");
        if (keys.length != frameSlots.length) {
            throw new IllegalArgumentException("variable keys and frame slots must have the same length");
        }
        this.keys = List.of(keys);
        this.frameSlots = frameSlots.clone();
        for (int index = 0; index < this.keys.size(); index++) {
            if (this.frameSlots[index] < 0) {
                throw new IllegalArgumentException("frame slots must not be negative");
            }
        }
    }

    public DefaultCalculationMemory freeze(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope");
        if (frameSlots.length == 0) {
            return DefaultCalculationMemory.emptyInstance();
        }
        return DefaultCalculationMemory.variables(keys, copyValues(scope));
    }

    List<VariableKey> keys() {
        return keys;
    }

    Object[] copyValues(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope");
        Object[] values = new Object[frameSlots.length];
        for (int index = 0; index < frameSlots.length; index++) {
            values[index] = scope.read(frameSlots[index]);
        }
        return values;
    }
}
