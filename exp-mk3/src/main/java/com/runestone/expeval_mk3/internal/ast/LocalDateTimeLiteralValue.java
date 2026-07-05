package com.runestone.expeval_mk3.internal.ast;

import java.time.LocalDateTime;
import java.util.Objects;

record LocalDateTimeLiteralValue(LocalDateTime value) implements LiteralValue {

    LocalDateTimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
