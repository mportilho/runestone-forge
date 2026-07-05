package com.runestone.expeval_mk3.internal.ast;

import java.time.LocalDate;
import java.util.Objects;

record DateLiteralValue(LocalDate value) implements LiteralValue {

    DateLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
