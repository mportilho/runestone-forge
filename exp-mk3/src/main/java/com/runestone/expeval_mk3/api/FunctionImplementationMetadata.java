package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Stable metadata that identifies the registered Java implementation handle.
 */
public record FunctionImplementationMetadata(
        String kind,
        String owner,
        String memberName,
        String methodType,
        String stableImplementationId) {

    public FunctionImplementationMetadata {
        kind = requireNonBlank(kind, "kind");
        owner = requireNonBlank(owner, "owner");
        memberName = requireNonBlank(memberName, "memberName");
        methodType = requireNonBlank(methodType, "methodType");
        stableImplementationId = requireNonBlank(stableImplementationId, "stableImplementationId");
    }

    public static FunctionImplementationMetadata forStaticMethod(Method method) {
        Objects.requireNonNull(method, "method");
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method must be static when no provider identity is supplied");
        }
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        String owner = method.getDeclaringClass().getName();
        String descriptor = methodType.toMethodDescriptorString();
        return new FunctionImplementationMetadata(
                "static-method",
                owner,
                method.getName(),
                descriptor,
                "static-method:" + owner + '#' + method.getName() + descriptor);
    }

    static FunctionImplementationMetadata forInstanceMethod(Method method, String providerId) {
        Objects.requireNonNull(method, "method");
        providerId = requireNonBlank(providerId, "providerId");
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method must be an instance method when provider identity is supplied");
        }
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        String owner = method.getDeclaringClass().getName();
        String descriptor = methodType.toMethodDescriptorString();
        return new FunctionImplementationMetadata(
                "instance-method",
                owner,
                method.getName(),
                descriptor,
                "instance-method:"
                        + owner
                        + '#'
                        + method.getName()
                        + descriptor
                        + "@provider:"
                        + providerId.length()
                        + ':'
                        + providerId);
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
