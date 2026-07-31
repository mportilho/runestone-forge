package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;

import java.util.Objects;

/** {@code .property} on a registered {@code ObjectType} receiver, invoked via the setup-resolved accessor handle. */
public record RegisteredPropertyExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        RegisteredPropertyNavigationBinding binding) implements ExecutableNode {

    public RegisteredPropertyExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(binding, "binding");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.registeredPropertyValue(receiver.execute(scope), safe, binding);
    }
}
