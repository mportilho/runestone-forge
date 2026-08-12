package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.BigDecimal;
import java.util.Objects;

public record AddDecimalExecutableNode(
        NodeId id, SourceSpan sourceSpan, ExecutableNode left, ExecutableNode right) implements ExecutableNode {

    public AddDecimalExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ((BigDecimal) left.execute(scope)).add((BigDecimal) right.execute(scope));
    }
}
