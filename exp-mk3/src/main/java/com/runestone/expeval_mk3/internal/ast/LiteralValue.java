package com.runestone.expeval_mk3.internal.ast;

sealed interface LiteralValue permits
        BigIntegerLiteralValue,
        BooleanLiteralValue,
        DateLiteralValue,
        DecimalLiteralValue,
        LocalDateTimeLiteralValue,
        LongLiteralValue,
        NullLiteralValue,
        OffsetDateTimeLiteralValue,
        StringLiteralValue,
        TimeLiteralValue {
}
