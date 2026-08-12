package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public record MultiplyDecimalExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode left,
        ExecutableNode right,
        MathContext mathContext) implements ExecutableNode {

    public MultiplyDecimalExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(mathContext, "mathContext");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ((BigDecimal) left.execute(scope)).multiply((BigDecimal) right.execute(scope), mathContext);
    }
}
