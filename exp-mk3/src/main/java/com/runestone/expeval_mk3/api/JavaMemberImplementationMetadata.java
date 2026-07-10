package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Stable metadata for a Java-backed property or method exposed to navigation.
 */
public record JavaMemberImplementationMetadata(
        String kind,
        String owner,
        String memberName,
        String methodType) {

    public JavaMemberImplementationMetadata {
        kind = requireNonBlank(kind, "kind");
        owner = requireNonBlank(owner, "owner");
        memberName = requireNonBlank(memberName, "memberName");
        methodType = requireNonBlank(methodType, "methodType");
    }

    public static JavaMemberImplementationMetadata forMethod(String kind, Method method) {
        kind = requireNonBlank(kind, "kind");
        Objects.requireNonNull(method, "method");
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return new JavaMemberImplementationMetadata(
                kind,
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
