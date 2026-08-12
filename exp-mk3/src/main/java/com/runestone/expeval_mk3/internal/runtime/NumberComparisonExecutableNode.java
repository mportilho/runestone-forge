package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The specialized order comparison for a {@code NUMBER} operand type resolved at plan-build time
 * (issue #126): {@code compareTo} runs directly against the two {@link BigDecimal} operands instead of
 * going through {@link ExpressionRuntime#compareValues}'s type re-check. {@code operator} is kept as
 * the resolved {@link BinaryOperator} itself, matching how {@link BinaryExecutableNode#comparison}
 * already carries it, rather than decomposed into a pair of booleans whose meaning only the
 * constructor call site would explain. {@link BinaryExecutableNode#comparison} remains the node the
 * Unoptimized Oracle builds for every order comparison, this operand type included.
 */
public record NumberComparisonExecutableNode(
        NodeId id, SourceSpan sourceSpan, BinaryOperator operator, ExecutableNode left, ExecutableNode right)
        implements ExecutableNode {

    public NumberComparisonExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        int comparison = ((BigDecimal) left.execute(scope)).compareTo((BigDecimal) right.execute(scope));
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
