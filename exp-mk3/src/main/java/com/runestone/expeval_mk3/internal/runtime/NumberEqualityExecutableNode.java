package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The specialized equality for a {@code NUMBER} operand type resolved at plan-build time (issue
 * #126): {@code compareTo}, not {@code equals}, so {@code 1.0 = 1} stays {@code true} exactly as
 * {@link ExpressionRuntime#structuralEquals} already requires for this type. {@code negated} is
 * resolved once from the operator ({@code <>} vs {@code =}) instead of being re-checked on every
 * evaluation. {@link BinaryExecutableNode#equality} remains the node the Unoptimized Oracle builds for
 * every equality comparison, this operand type included.
 */
public record NumberEqualityExecutableNode(NodeId id, SourceSpan sourceSpan, boolean negated, ExecutableNode left, ExecutableNode right)
        implements ExecutableNode {

    public NumberEqualityExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        BigDecimal leftValue = (BigDecimal) left.execute(scope);
        BigDecimal rightValue = (BigDecimal) right.execute(scope);
        return (leftValue.compareTo(rightValue) == 0) != negated;
    }
}
