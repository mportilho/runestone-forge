package com.runestone.expeval_mk3.internal.semantics;

import java.time.LocalDateTime;
import java.util.Objects;

public record PreparedOffsetDateTimeLiteralValue(LocalDateTime normalizedLocalDateTime)
        implements PreparedSemanticValue {

    public PreparedOffsetDateTimeLiteralValue {
        Objects.requireNonNull(normalizedLocalDateTime, "normalizedLocalDateTime");
    }
}
