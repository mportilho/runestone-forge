package com.runestone.expeval2.runtime;

import com.runestone.expeval2.semantic.ResolvedType;
import com.runestone.expeval2.semantic.ScalarType;

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
