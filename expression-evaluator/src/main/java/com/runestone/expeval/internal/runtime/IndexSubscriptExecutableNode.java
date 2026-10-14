package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;

import java.math.BigInteger;
import java.util.Objects;

/**
 * {@code [i]} on a list receiver: resolves a possibly-negative literal index against the receiver's size.
 * Per ADR 0018 {@code safe} tolerates an out-of-range index by yielding null; the strict form fails.
 */
public record IndexSubscriptExecutableNode(
        NodeId id, SourceSpan sourceSpan, ExecutableNode receiver, BigInteger index, boolean safe)
        implements ExecutableNode {

    public IndexSubscriptExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(index, "index");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.indexedValue(receiver.execute(scope), index, safe, sourceSpan);
    }
}
