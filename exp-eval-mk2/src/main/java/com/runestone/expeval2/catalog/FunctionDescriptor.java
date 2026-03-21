package com.runestone.expeval2.catalog;

import com.runestone.expeval2.types.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public final class FunctionDescriptor {

    private final String name;
    private final List<Class<?>> parameterTypes;
    private final List<ResolvedType> parameterResolvedTypes;
    private final ResolvedType returnType;
    private final MethodHandle invoker;
    private final MethodHandle compiledInvoker;
    private final FunctionRef functionRef;
    private final boolean foldable;

    public FunctionDescriptor(
        String name,
        List<Class<?>> parameterTypes,
        List<ResolvedType> parameterResolvedTypes,
        ResolvedType returnType,
        MethodHandle invoker
    ) {
        this(name, parameterTypes, parameterResolvedTypes, returnType, invoker, false);
    }

    public FunctionDescriptor(
        String name,
        List<Class<?>> parameterTypes,
        List<ResolvedType> parameterResolvedTypes,
        ResolvedType returnType,
        MethodHandle invoker,
        boolean foldable
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes must not be null"));
        this.parameterResolvedTypes = List.copyOf(Objects.requireNonNull(parameterResolvedTypes, "parameterResolvedTypes must not be null"));
        this.returnType = Objects.requireNonNull(returnType, "returnType must not be null");
        this.invoker = Objects.requireNonNull(invoker, "invoker must not be null");
        if (this.parameterTypes.size() != this.parameterResolvedTypes.size()) {
            throw new IllegalArgumentException("parameterResolvedTypes must match parameterTypes size");
        }
        this.compiledInvoker = compileInvoker(invoker, this.parameterTypes.size());
        this.functionRef = new FunctionRef(this.name, this.parameterTypes.size());
        this.foldable = foldable;
    }

    public String name() {
        return name;
    }

    public List<Class<?>> parameterTypes() {
        return parameterTypes;
    }

    public List<ResolvedType> parameterResolvedTypes() {
        return parameterResolvedTypes;
    }

    public ResolvedType returnType() {
        return returnType;
    }

    public MethodHandle invoker() {
        return invoker;
    }

    public int arity() {
        return parameterTypes.size();
    }

    public FunctionRef functionRef() {
        return functionRef;
    }

    public boolean isFoldable() {
        return foldable;
    }

    public Object invoke(Object[] arguments) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        if (arguments.length != arity()) {
            throw new IllegalArgumentException("arguments size does not match function arity");
        }
        try {
            return compiledInvoker.invokeExact(arguments);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new IllegalStateException("failed to invoke function '" + name + "'", throwable);
        }
    }

    private static MethodHandle compileInvoker(MethodHandle invoker, int arity) {
        return invoker
            .asType(invoker.type().generic())
            .asSpreader(Object[].class, arity);
    }
}
