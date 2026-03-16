package com.runestone.expeval2.runtime;

import com.runestone.expeval2.semantic.ResolvedType;
import com.runestone.expeval2.semantic.VectorType;

import java.util.List;

public record VectorValue(List<RuntimeValue> elements) implements RuntimeValue {

    public VectorValue {
        elements = List.copyOf(elements);
    }

    @Override
    public ResolvedType type() {
        return VectorType.INSTANCE;
    }

    @Override
    public Object raw() {
        return elements;
    }
}
