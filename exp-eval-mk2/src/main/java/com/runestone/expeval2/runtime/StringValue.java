package com.runestone.expeval2.runtime;

import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;

import java.util.Objects;

public record StringValue(String value) implements RuntimeValue {

    public StringValue {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public ResolvedType type() {
        return ScalarType.STRING;
    }

    @Override
    public Object raw() {
        return value;
    }
}
