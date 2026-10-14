package com.runestone.expeval.internal.memory;

import com.runestone.expeval.api.VariableKey;
import com.runestone.expeval.api.VariableOrigin;
import com.runestone.expeval.internal.runtime.ExecutionScope;

import java.util.List;
import java.util.Objects;

/** Standalone variable metadata that cannot retain the executable plan or an execution frame. */
public final class VariableMemorySchema {

    private static final int SCHEMA_UNITS = 80;
    private static final int LIST_UNITS = 48;
    private static final int REFERENCE_UNITS = 8;
    private static final int VARIABLE_KEY_UNITS = 48;
    private static final int ARRAY_UNITS = 32;
    private static final int STRING_UNITS = 48;

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

    int estimatedRetainedWeight() {
        long units = SCHEMA_UNITS;
        units += LIST_UNITS + (long) keys.size() * REFERENCE_UNITS;
        for (VariableKey key : keys) {
            units += VARIABLE_KEY_UNITS;
            if (key.origin() == VariableOrigin.INTERNAL) {
                units += STRING_UNITS + (long) key.name().length() * Character.BYTES;
            }
        }
        units += ARRAY_UNITS + (long) frameSlots.length * Integer.BYTES;
        return (int) Math.min(Integer.MAX_VALUE, units);
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
