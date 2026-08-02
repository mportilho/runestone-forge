package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/**
 * {@code [?(predicate)]} on a list receiver. The predicate runs once per element with the current-item
 * frame slot pushed to that element and restored afterward, on both a matching and a failing predicate.
 */
public record FilterExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        ExecutableNode predicate,
        int currentItemFrameSlot,
        int maxMaterializedSize) implements ExecutableNode {

    public FilterExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(predicate, "predicate");
        if (currentItemFrameSlot < 0) {
            throw new IllegalArgumentException("currentItemFrameSlot must not be negative");
        }
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.filteredValues(
                receiver.execute(scope), safe, predicate, currentItemFrameSlot, scope, maxMaterializedSize, sourceSpan);
    }
}
