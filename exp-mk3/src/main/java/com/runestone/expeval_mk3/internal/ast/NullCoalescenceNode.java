package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record NullCoalescenceNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<ExpressionNode> operands,
        List<SourceSpan> operatorSpans) implements ExpressionNode {

    NullCoalescenceNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operands, "operands");
        Objects.requireNonNull(operatorSpans, "operatorSpans");
        operands = List.copyOf(operands);
        operatorSpans = List.copyOf(operatorSpans);
        if (operands.size() < 2) {
            throw new IllegalArgumentException("operands must contain at least two expressions");
        }
        if (operatorSpans.size() != operands.size() - 1) {
            throw new IllegalArgumentException("operatorSpans must contain one fewer entry than operands");
        }
    }
}
