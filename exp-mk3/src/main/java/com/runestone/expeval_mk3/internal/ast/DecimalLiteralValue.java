package com.runestone.expeval_mk3.internal.ast;

import java.math.BigDecimal;
import java.util.Objects;

record DecimalLiteralValue(BigDecimal value) implements LiteralValue {

    DecimalLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
