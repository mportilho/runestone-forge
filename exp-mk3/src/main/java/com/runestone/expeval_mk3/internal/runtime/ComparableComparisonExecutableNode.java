package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/**
 * The specialized order comparison for every orderable operand type other than {@code NUMBER}
 * (issue #126) — {@code STRING}, {@code DATE}, {@code TIME}, {@code DATETIME} — all natural-ordering
 * {@link Comparable} values whose runtime class is already known from the resolved operand type, so
 * the cast is direct instead of going through {@link ExpressionRuntime#compareValues}'s type re-check.
 * {@code operator} is kept as the resolved {@link BinaryOperator} itself, matching how
 * {@link BinaryExecutableNode#comparison} already carries it, rather than decomposed into a pair of
 * booleans whose meaning only the constructor call site would explain.
 * {@link BinaryExecutableNode#comparison} remains the node the Unoptimized Oracle builds for every
 * order comparison, these operand types included.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public record ComparableComparisonExecutableNode(
        NodeId id, SourceSpan sourceSpan, BinaryOperator operator, ExecutableNode left, ExecutableNode right)
        implements ExecutableNode {

    public ComparableComparisonExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        int comparison = ((Comparable) left.execute(scope)).compareTo(right.execute(scope));
        return switch (operator) {
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
            default -> throw new IllegalStateException(
                    "internal invariant: unexpected order comparison operator " + operator);
        };
    }
}
