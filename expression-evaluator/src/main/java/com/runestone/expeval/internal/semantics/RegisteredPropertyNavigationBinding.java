package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.ExpressionType;
import com.runestone.expeval.api.JavaMemberImplementationMetadata;
import com.runestone.expeval.api.JavaPropertyDescriptor;
import com.runestone.expeval.api.RuntimeNullability;

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
