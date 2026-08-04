package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;
import java.util.Set;

/**
 * The downloaded representation {@code in} uses for a constant {@code STRING}/{@code BOOLEAN}/temporal
 * collection at or above the membership download threshold (issue #119): a hash-based lookup, matching
 * the {@code equals}-based comparison {@link ExpressionRuntime#structuralEquals} uses for these element
 * types exactly.
 */
public record HashLookupMembershipExecutableNode(
        NodeId id, SourceSpan sourceSpan, boolean negated, ExecutableNode element, Set<Object> lookup)
        implements ExecutableNode {

    public HashLookupMembershipExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(lookup, "lookup");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return lookup.contains(element.execute(scope)) != negated;
    }
}
