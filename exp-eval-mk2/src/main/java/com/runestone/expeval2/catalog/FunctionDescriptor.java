package com.runestone.expeval2.catalog;

import com.runestone.expeval2.semantic.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public record FunctionDescriptor(
    String name,
    List<Class<?>> parameterTypes,
    List<ResolvedType> parameterResolvedTypes,
    ResolvedType returnType,
    MethodHandle invoker
) {

    public FunctionDescriptor {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes must not be null"));
        parameterResolvedTypes = List.copyOf(Objects.requireNonNull(parameterResolvedTypes, "parameterResolvedTypes must not be null"));
        Objects.requireNonNull(returnType, "returnType must not be null");
        Objects.requireNonNull(invoker, "invoker must not be null");
        if (parameterTypes.size() != parameterResolvedTypes.size()) {
            throw new IllegalArgumentException("parameterResolvedTypes must match parameterTypes size");
        }
    }

    public int arity() {
        return parameterTypes.size();
    }

    public FunctionRef functionRef() {
        return new FunctionRef(name, arity());
    }

    public Object invoke(List<Object> arguments) {
        List<Object> safeArguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        if (safeArguments.size() != arity()) {
            throw new IllegalArgumentException("arguments size does not match function arity");
        }
        try {
            return invoker.invokeWithArguments(safeArguments);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new IllegalStateException("failed to invoke function '" + name + "'", throwable);
        }
    }
}
