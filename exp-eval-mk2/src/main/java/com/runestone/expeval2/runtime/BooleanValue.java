package com.runestone.expeval2.runtime;

import com.runestone.expeval2.semantic.ResolvedType;
import com.runestone.expeval2.semantic.ScalarType;

public record BooleanValue(boolean value) implements RuntimeValue {

    @Override
    public ResolvedType type() {
        return ScalarType.BOOLEAN;
    }

    @Override
    public Object raw() {
        return value;
    }
}
