package com.runestone.expeval.catalog;

import com.runestone.expeval.types.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public record MethodDescriptor(
        String name,
        MethodHandle handle,
        List<Class<?>> parameterTypes,
        ResolvedType returnType
) {

    public MethodDescriptor {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(handle, "handle must not be null");
        parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes must not be null"));
        Objects.requireNonNull(returnType, "returnType must not be null");
    }

    public int arity() {
        return parameterTypes.size();
    }
}
