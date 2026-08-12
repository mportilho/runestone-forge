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
    private final InvocationEntryPoint entryPoint;
    private final JavaMemberImplementationMetadata implementationMetadata;
    private final FunctionPurity purity;

    JavaMethodDescriptor(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            MethodHandle invocationHandle,
            JavaMemberImplementationMetadata implementationMetadata,
            FunctionPurity purity) {
        this.languageName = FunctionSignature.validateLanguageName(languageName);
        this.parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.invocationHandle = Objects.requireNonNull(invocationHandle, "invocationHandle");
        this.entryPoint = InvocationEntryPoint.prepare(invocationHandle);
        this.implementationMetadata = Objects.requireNonNull(implementationMetadata, "implementationMetadata");
        this.purity = Objects.requireNonNull(purity, "purity");
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

    public Object invoke(Object receiver) throws Throwable {
        return entryPoint.invoke(Objects.requireNonNull(receiver, "receiver"));
    }

    public Object invoke(Object receiver, Object argument0) throws Throwable {
        return entryPoint.invoke(
                Objects.requireNonNull(receiver, "receiver"),
                Objects.requireNonNull(argument0, "argument0"));
    }

    public Object invoke(Object receiver, Object argument0, Object argument1) throws Throwable {
        return entryPoint.invoke(
                Objects.requireNonNull(receiver, "receiver"),
                Objects.requireNonNull(argument0, "argument0"),
                Objects.requireNonNull(argument1, "argument1"));
    }

    public Object invoke(Object receiver, Object argument0, Object argument1, Object argument2) throws Throwable {
        return entryPoint.invoke(
                Objects.requireNonNull(receiver, "receiver"),
                Objects.requireNonNull(argument0, "argument0"),
                Objects.requireNonNull(argument1, "argument1"),
                Objects.requireNonNull(argument2, "argument2"));
    }

    public Object invokeArray(Object[] receiverAndArguments) throws Throwable {
        Objects.requireNonNull(receiverAndArguments, "receiverAndArguments");
        if (receiverAndArguments.length != parameterTypes.size() + 1) {
            throw new IllegalArgumentException("receiver and argument count must match method arity");
        }
        for (int index = 0; index < receiverAndArguments.length; index++) {
            Objects.requireNonNull(receiverAndArguments[index], "receiverAndArguments[" + index + "]");
        }
        return entryPoint.invokeArray(receiverAndArguments);
    }

    public JavaMemberImplementationMetadata implementationMetadata() {
        return implementationMetadata;
    }

    public FunctionPurity purity() {
        return purity;
    }

    public boolean pure() {
        return purity.pure();
    }

    public int arity() {
        return parameterTypes.size();
    }

    public FunctionSignature signature() {
        return new FunctionSignature(languageName, parameterTypes);
    }
}
