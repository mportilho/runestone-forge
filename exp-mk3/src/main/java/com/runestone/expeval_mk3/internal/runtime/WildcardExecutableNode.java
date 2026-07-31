package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.WildcardNavigationBinding;

import java.util.Objects;

/** {@code [*]} on a collection, map, or registered object receiver. */
public record WildcardExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        WildcardNavigationBinding binding,
        int maxMaterializedSize) implements ExecutableNode {

    public WildcardExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(binding, "binding");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.wildcardValues(receiver.execute(scope), safe, binding, maxMaterializedSize);
    }
}
