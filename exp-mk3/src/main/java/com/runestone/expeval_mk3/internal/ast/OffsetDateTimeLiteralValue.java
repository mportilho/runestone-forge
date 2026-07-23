package com.runestone.expeval_mk3.internal.ast;

import java.time.OffsetDateTime;
import java.util.Objects;

public record OffsetDateTimeLiteralValue(OffsetDateTime value) implements LiteralValue {

    public OffsetDateTimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
