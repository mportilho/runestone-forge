package com.runestone.expeval2.internal.semantic;

import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.catalog.FunctionRef;
import com.runestone.expeval2.types.ResolvedType;

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
