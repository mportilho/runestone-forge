package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.types.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public record ResolvedMemberBinding(
        String name,
        Kind kind,
        MethodHandle handle,
        List<Class<?>> parameterTypes,
        ResolvedType returnType,
        boolean safe
) {

    public ResolvedMemberBinding {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(handle, "handle must not be null");
        Objects.requireNonNull(parameterTypes, "parameterTypes must not be null");
        Objects.requireNonNull(returnType, "returnType must not be null");
        if (kind == Kind.PROPERTY && !parameterTypes.isEmpty()) {
            throw new IllegalArgumentException("property bindings must not declare parameter types");
        }
    }

    public static ResolvedMemberBinding property(String name, MethodHandle getter, ResolvedType returnType, boolean safe) {
        return new ResolvedMemberBinding(name, Kind.PROPERTY, getter, List.of(), returnType, safe);
    }

    public static ResolvedMemberBinding method(
            String name,
            MethodHandle handle,
            List<Class<?>> parameterTypes,
            ResolvedType returnType,
            boolean safe) {
        return new ResolvedMemberBinding(name, Kind.METHOD, handle, parameterTypes, returnType, safe);
    }

    public enum Kind {
        PROPERTY,
        METHOD
    }
}
