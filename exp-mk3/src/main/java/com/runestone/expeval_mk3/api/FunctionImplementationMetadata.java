package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Stable metadata that identifies the registered Java implementation handle.
 */
public record FunctionImplementationMetadata(
        String kind,
        String owner,
        String memberName,
        String methodType) {

    public FunctionImplementationMetadata {
        kind = requireNonBlank(kind, "kind");
        owner = requireNonBlank(owner, "owner");
        memberName = requireNonBlank(memberName, "memberName");
        methodType = requireNonBlank(methodType, "methodType");
    }

    public static FunctionImplementationMetadata forMethod(Method method) {
        Objects.requireNonNull(method, "method");
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return new FunctionImplementationMetadata(
                "method",
                method.getDeclaringClass().getName(),
                method.getName(),
                methodType.toMethodDescriptorString());
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
