package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;
import java.util.List;
import java.util.Objects;

public record NullCoalesceNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<ExpressionNode> operands,
        List<SourceSpan> operatorSpans) implements ExpressionNode {

    public NullCoalesceNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operands, "operands");
        operands = List.copyOf(operands);
        Objects.requireNonNull(operatorSpans, "operatorSpans");
        operatorSpans = List.copyOf(operatorSpans);
        if (operands.size() < 2) {
            throw new IllegalArgumentException("null coalescence needs at least two operands");
        }
        if (operatorSpans.size() != operands.size() - 1) {
            throw new IllegalArgumentException("null coalescence operator spans must sit between operands");
        }
    }
}
