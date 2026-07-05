package com.runestone.expeval_mk3.internal.ast;

import java.time.OffsetDateTime;
import java.util.Objects;

record OffsetDateTimeLiteralValue(OffsetDateTime value) implements LiteralValue {

    OffsetDateTimeLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
