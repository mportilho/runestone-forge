package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

/**
 * Java-backed method member exposed for object navigation invocation.
 */
public final class JavaMethodDescriptor {

    private final String languageName;
    private final List<ExpressionType> parameterTypes;
    private final ExpressionType returnType;
    private final MethodHandle invocationHandle;
    private final JavaMemberImplementationMetadata implementationMetadata;

    JavaMethodDescriptor(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            MethodHandle invocationHandle,
            JavaMemberImplementationMetadata implementationMetadata) {
        this.languageName = FunctionSignature.validateLanguageName(languageName);
        this.parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.invocationHandle = Objects.requireNonNull(invocationHandle, "invocationHandle");
        this.implementationMetadata = Objects.requireNonNull(implementationMetadata, "implementationMetadata");
        if (invocationHandle.type().parameterCount() != this.parameterTypes.size() + 1) {
            throw new IllegalArgumentException("invocation handle arity must match receiver plus parameter types");
        }
    }

    public String languageName() {
        return languageName;
    }

    public List<ExpressionType> parameterTypes() {
        return parameterTypes;
    }

    public ExpressionType returnType() {
        return returnType;
    }

    public MethodHandle invocationHandle() {
        return invocationHandle;
    }

    public JavaMemberImplementationMetadata implementationMetadata() {
        return implementationMetadata;
    }

    public int arity() {
        return parameterTypes.size();
    }

    public FunctionSignature signature() {
        return new FunctionSignature(languageName, parameterTypes);
    }
}
