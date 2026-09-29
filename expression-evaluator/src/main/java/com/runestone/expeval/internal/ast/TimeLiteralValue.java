package com.runestone.expeval.internal.ast;

import java.time.LocalTime;
import java.util.Objects;

public record TimeLiteralValue(LocalTime value) implements LiteralValue {

    public TimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
