package com.runestone.expeval2.runtime;

import com.runestone.expeval2.semantic.ResolvedType;
import com.runestone.expeval2.semantic.UnknownType;

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
