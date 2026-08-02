package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.BigInteger;
import java.util.Objects;

/**
 * {@code [a:b]}/{@code [a:]}/{@code [:b]} on a list receiver. {@code startBound}/{@code endBound} are
 * {@code null} for an unbounded side; normalization against the receiver's runtime size happens on
 * every execution, matching the AST's slice-bound semantics without retaining the AST bound shape.
 */
public record SliceSubscriptExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        BigInteger startBound,
        BigInteger endBound,
        boolean safe,
        int maxMaterializedSize) implements ExecutableNode {

    public SliceSubscriptExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.slicedValues(
                receiver.execute(scope), startBound, endBound, safe, maxMaterializedSize, sourceSpan);
    }
}
