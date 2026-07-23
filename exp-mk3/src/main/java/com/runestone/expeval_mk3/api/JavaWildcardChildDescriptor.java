package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * Java-backed child member explicitly exposed to object wildcard navigation.
 */
public final class JavaWildcardChildDescriptor {

    private final String name;
    private final ExpressionType type;
    private final MethodHandle accessorHandle;
    private final JavaMemberImplementationMetadata implementationMetadata;

    JavaWildcardChildDescriptor(
            String name,
            ExpressionType type,
            MethodHandle accessorHandle,
            JavaMemberImplementationMetadata implementationMetadata) {
        this.name = FunctionSignature.validateLanguageName(name);
        this.type = Objects.requireNonNull(type, "type");
        this.accessorHandle = Objects.requireNonNull(accessorHandle, "accessorHandle");
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

    public JavaMemberImplementationMetadata implementationMetadata() {
        return implementationMetadata;
    }
}
