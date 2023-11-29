package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.operation.types.*;

public enum ComparisonOperation {

    EQ(Equals.class),
    LT(Less.class),
    LE(LessOrEquals.class),
    GT(Greater.class),
    GE(GreaterOrEquals.class),
    LK(Like.class),
    SW(StartsWith.class),
    EW(EndsWith.class),
    IN(IsIn.class),
    BT(Between.class)
    ;

    private final Class<? super DefinedFilterOperation> operation;

    ComparisonOperation(Class<? super DefinedFilterOperation> operation) {
        this.operation = operation;
    }

    public Class<? super DefinedFilterOperation> getOperation() {
        return operation;
    }
}
