package com.runestone.expeval.catalog;

import com.runestone.expeval.api.FunctionInvocationException;
import com.runestone.expeval.types.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public final class FunctionDescriptor {

    private final String name;
    private final List<Class<?>> parameterTypes;
    private final List<ResolvedType> parameterResolvedTypes;
    private final ResolvedType returnType;
    private final MethodHandle invoker;
    private final MethodHandle genericInvoker;
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
        int arity = this.parameterTypes.size();
        this.genericInvoker = invoker.asType(java.lang.invoke.MethodType.genericMethodType(arity));
        this.compiledInvoker = compileInvoker(invoker, arity);
        this.functionRef = new FunctionRef(this.name, arity);
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

    public Object invoke() {
        try {
            return genericInvoker.invokeExact();
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object a1) {
        try {
            return genericInvoker.invokeExact(a1);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object a1, Object a2) {
        try {
            return genericInvoker.invokeExact(a1, a2);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object a1, Object a2, Object a3) {
        try {
            return genericInvoker.invokeExact(a1, a2, a3);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4) {
        try {
            return genericInvoker.invokeExact(a1, a2, a3, a4);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5) {
        try {
            return genericInvoker.invokeExact(a1, a2, a3, a4, a5);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) {
        try {
            return genericInvoker.invokeExact(a1, a2, a3, a4, a5, a6);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    public Object invoke(Object[] arguments) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        if (arguments.length != arity()) {
            throw new IllegalArgumentException("arguments size does not match function arity");
        }
        try {
            return compiledInvoker.invokeExact(arguments);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            throw new FunctionInvocationException(name, throwable);
        }
    }

    private static MethodHandle compileInvoker(MethodHandle invoker, int arity) {
        return invoker
            .asType(invoker.type().generic())
            .asSpreader(Object[].class, arity);
    }
}
