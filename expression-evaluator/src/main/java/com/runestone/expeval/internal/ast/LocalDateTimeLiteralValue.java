package com.runestone.expeval.internal.ast;

import java.time.LocalDateTime;
import java.util.Objects;

public record LocalDateTimeLiteralValue(LocalDateTime value) implements LiteralValue {

    public LocalDateTimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
