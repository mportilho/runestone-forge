package com.runestone.expeval2.runtime.values;

import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;

import java.math.BigDecimal;
import java.util.Objects;

public record NumberValue(BigDecimal value) implements RuntimeValue {

    public NumberValue {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public ResolvedType type() {
        return ScalarType.NUMBER;
    }

    @Override
    public Object raw() {
        return value;
    }
}
