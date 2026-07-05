package com.runestone.expeval_mk3.internal.ast;

import java.time.LocalTime;
import java.util.Objects;

record TimeLiteralValue(LocalTime value) implements LiteralValue {

    TimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
