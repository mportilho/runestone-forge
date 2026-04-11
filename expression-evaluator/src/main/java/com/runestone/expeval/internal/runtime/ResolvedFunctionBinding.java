package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.catalog.FunctionRef;
import com.runestone.expeval.types.ResolvedType;

import java.util.Objects;

public record ResolvedFunctionBinding(
    FunctionRef functionRef,
    FunctionDescriptor descriptor,
    ResolvedType returnType
) {

    public ResolvedFunctionBinding {
        Objects.requireNonNull(functionRef, "functionRef must not be null");
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        Objects.requireNonNull(returnType, "returnType must not be null");
    }
}
