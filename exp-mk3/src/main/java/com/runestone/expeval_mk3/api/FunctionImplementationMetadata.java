package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Descriptive metadata for a registered Java implementation handle.
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

    public static FunctionImplementationMetadata forStaticMethod(Method method) {
        Objects.requireNonNull(method, "method");
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method must be static");
        }
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        String owner = method.getDeclaringClass().getName();
        String descriptor = methodType.toMethodDescriptorString();
        return new FunctionImplementationMetadata(
                "static-method",
                owner,
                method.getName(),
                descriptor);
    }

    static FunctionImplementationMetadata forInstanceMethod(Method method) {
        Objects.requireNonNull(method, "method");
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method must be an instance method");
        }
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        String owner = method.getDeclaringClass().getName();
        String descriptor = methodType.toMethodDescriptorString();
        return new FunctionImplementationMetadata(
                "instance-method",
                owner,
                method.getName(),
                descriptor);
    }

    String describeImplementation() {
        return owner + '#' + memberName + methodType;
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
