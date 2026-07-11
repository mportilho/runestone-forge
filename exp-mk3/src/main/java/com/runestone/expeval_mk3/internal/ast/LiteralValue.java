package com.runestone.expeval_mk3.internal.ast;

sealed interface LiteralValue permits
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
