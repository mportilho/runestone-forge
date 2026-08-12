package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/**
 * The specialized equality for every operand type whose structural equality coincides with
 * {@code equals} (issue #126) — {@code STRING}, {@code BOOLEAN}, {@code DATE}, {@code TIME},
 * {@code DATETIME} — matching {@link ExpressionRuntime#structuralEquals}'s own fallback for these
 * types exactly. {@code negated} is resolved once from the operator ({@code <>} vs {@code =}) instead
 * of being re-checked on every evaluation. A {@code COLLECTION} or {@code MAP} operand type never
 * builds this node: {@link BinaryExecutableNode#equality} is kept for both, because the outer
 * dispatch this node exists to skip only costs one {@code instanceof} check against the recursive
 * element/value walk {@link ExpressionRuntime#structuralEquals} already performs — the same
 * "does not pay for itself" call the issue #119 membership download made for collection and map
 * elements. {@link BinaryExecutableNode#equality} remains the node the Unoptimized Oracle builds for
 * every equality comparison.
 */
public record EqualsEqualityExecutableNode(NodeId id, SourceSpan sourceSpan, boolean negated, ExecutableNode left, ExecutableNode right)
        implements ExecutableNode {

    public EqualsEqualityExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return left.execute(scope).equals(right.execute(scope)) != negated;
    }
}
