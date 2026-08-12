package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.JavaMemberImplementationMetadata;
import com.runestone.expeval_mk3.api.JavaMethodDescriptor;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * Selects the setup-time resolved invocation handle for a {@code CallNavigationLink} whose receiver
 * is a registered {@code ObjectType}. A method is a navigation binding, not a function binding:
 * overload selection already happened against the registered {@code JavaTypeDescriptor} at semantic
 * resolution time, by exact argument-type signature.
 */
public record RegisteredMethodNavigationBinding(
        ExpressionType receiverType,
        ExpressionType resultType,
        RuntimeNullability resultNullability,
        JavaMethodDescriptor descriptor,
        boolean pure) implements NavigationBinding {

    public RegisteredMethodNavigationBinding {
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(resultNullability, "resultNullability");
        Objects.requireNonNull(descriptor, "descriptor");
    }

    public MethodHandle invocationHandle() {
        return descriptor.invocationHandle();
    }

    public JavaMemberImplementationMetadata implementationMetadata() {
        return descriptor.implementationMetadata();
    }
}
