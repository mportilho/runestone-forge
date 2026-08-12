package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.JavaMemberImplementationMetadata;
import com.runestone.expeval_mk3.api.JavaPropertyDescriptor;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * Selects the setup-time resolved accessor for a {@code PropertyNavigationLink} whose receiver is a
 * registered {@code ObjectType}. Planning invokes {@link #accessorHandle()} directly instead of
 * looking the member up again by name.
 */
public record RegisteredPropertyNavigationBinding(
        ExpressionType receiverType,
        ExpressionType resultType,
        RuntimeNullability resultNullability,
        JavaPropertyDescriptor descriptor,
        boolean pure) implements NavigationBinding {

    public RegisteredPropertyNavigationBinding {
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(resultNullability, "resultNullability");
        Objects.requireNonNull(descriptor, "descriptor");
    }

    public MethodHandle accessorHandle() {
        return descriptor.accessorHandle();
    }

    public JavaMemberImplementationMetadata implementationMetadata() {
        return descriptor.implementationMetadata();
    }
}
