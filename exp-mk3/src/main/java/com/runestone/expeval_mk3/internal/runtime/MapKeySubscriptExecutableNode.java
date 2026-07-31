package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/** {@code ["key"]} on a map receiver; {@code safe} protects only a null receiver, not a missing key. */
public record MapKeySubscriptExecutableNode(
        NodeId id, SourceSpan sourceSpan, ExecutableNode receiver, String key, boolean safe)
        implements ExecutableNode {

    public MapKeySubscriptExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(key, "key");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.mapKeyValue(receiver.execute(scope), key, safe, sourceSpan);
    }
}
