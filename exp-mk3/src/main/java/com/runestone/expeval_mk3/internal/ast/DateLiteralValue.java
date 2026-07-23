package com.runestone.expeval_mk3.internal.ast;

import java.time.LocalDate;
import java.util.Objects;

public record DateLiteralValue(LocalDate value) implements LiteralValue {

    public DateLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
