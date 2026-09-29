package com.runestone.expeval.internal.ast;

public sealed interface LiteralValue permits
        BigIntegerLiteralValue,
        BooleanLiteralValue,
        DateLiteralValue,
        DecimalLiteralValue,
        LocalDateTimeLiteralValue,
        LongLiteralValue,
        OffsetDateTimeLiteralValue,
        StringLiteralValue,
        TimeLiteralValue {
}
