package com.runestone.expeval2.runtime;

import com.runestone.expeval2.semantic.ResolvedType;
import com.runestone.expeval2.semantic.ScalarType;

import java.time.LocalTime;
import java.util.Objects;

public record TimeValue(LocalTime value) implements RuntimeValue {

    public TimeValue {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public ResolvedType type() {
        return ScalarType.TIME;
    }

    @Override
    public Object raw() {
        return value;
    }
}
