package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The downloaded representation {@code in} uses for a constant {@code NUMBER} collection at or above
 * the membership download threshold (issue #119): a sorted, immutable list searched by {@code compareTo}
 * (natural ordering), the same comparison {@link ExpressionRuntime#structuralEquals} uses for
 * {@code NUMBER}. A {@code HashSet<BigDecimal>} is never used here, because {@code 1.0} and {@code 1}
 * are equal by {@code compareTo} and unequal by {@code equals}/hash, which would change what {@code in}
 * returns.
 */
public record SortedNumberMembershipExecutableNode(
        NodeId id, SourceSpan sourceSpan, boolean negated, ExecutableNode element, List<BigDecimal> sortedElements)
        implements ExecutableNode {

    public SortedNumberMembershipExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(sortedElements, "sortedElements");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        BigDecimal value = (BigDecimal) element.execute(scope);
        boolean contains = Collections.binarySearch(sortedElements, value) >= 0;
        return contains != negated;
    }
}
