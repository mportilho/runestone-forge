package com.runestone.expeval2.runtime;

import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;

import java.time.LocalDateTime;
import java.util.Objects;

public record DateTimeValue(LocalDateTime value) implements RuntimeValue {

    public DateTimeValue {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public ResolvedType type() {
        return ScalarType.DATETIME;
    }

    @Override
    public Object raw() {
        return value;
    }
}
