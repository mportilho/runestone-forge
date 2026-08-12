package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;

import java.util.Objects;

/** Unoptimized Oracle route retaining generic MethodHandle property invocation. */
public record OracleRegisteredPropertyExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        RegisteredPropertyNavigationBinding binding) implements ExecutableNode {

    public OracleRegisteredPropertyExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(binding, "binding");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.oracleRegisteredPropertyValue(receiver.execute(scope), safe, binding, sourceSpan);
    }
}
