package com.runestone.expeval_mk3.internal.ast;

import java.time.LocalTime;
import java.util.Objects;

public record TimeLiteralValue(LocalTime value) implements LiteralValue {

    public TimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
