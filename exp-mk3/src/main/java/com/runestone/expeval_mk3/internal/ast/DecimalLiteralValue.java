package com.runestone.expeval_mk3.internal.ast;

import java.math.BigDecimal;
import java.util.Objects;

public record DecimalLiteralValue(BigDecimal value) implements LiteralValue {

    public DecimalLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
