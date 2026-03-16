package com.runestone.expeval2.runtime;

import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.UnknownType;

public enum NullValue implements RuntimeValue {
    INSTANCE;

    @Override
    public ResolvedType type() {
        return UnknownType.INSTANCE;
    }

    @Override
    public Object raw() {
        return null;
    }
}
