package com.runestone.expeval2.runtime;

import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;

import java.time.LocalDate;
import java.util.Objects;

public record DateValue(LocalDate value) implements RuntimeValue {

    public DateValue {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public ResolvedType type() {
        return ScalarType.DATE;
    }

    @Override
    public Object raw() {
        return value;
    }
}
