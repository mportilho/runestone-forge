package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * Java-backed property member exposed for object navigation.
 */
public final class JavaPropertyDescriptor {

    private final String name;
    private final ExpressionType type;
    private final MethodHandle accessorHandle;
    private final InvocationEntryPoint entryPoint;
    private final JavaMemberImplementationMetadata implementationMetadata;

    JavaPropertyDescriptor(
            String name,
            ExpressionType type,
            MethodHandle accessorHandle,
            JavaMemberImplementationMetadata implementationMetadata) {
        this.name = FunctionSignature.validateLanguageName(name);
        this.type = Objects.requireNonNull(type, "type");
        this.accessorHandle = Objects.requireNonNull(accessorHandle, "accessorHandle");
        this.entryPoint = InvocationEntryPoint.prepare(accessorHandle);
        this.implementationMetadata = Objects.requireNonNull(implementationMetadata, "implementationMetadata");
    }

    public String name() {
        return name;
    }

    public ExpressionType type() {
        return type;
    }

    public MethodHandle accessorHandle() {
        return accessorHandle;
    }

    public Object invoke(Object receiver) throws Throwable {
        return entryPoint.invoke(Objects.requireNonNull(receiver, "receiver"));
    }

    public JavaMemberImplementationMetadata implementationMetadata() {
        return implementationMetadata;
    }
}
