package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;

import java.util.Objects;

public record BinaryNullCoalesceExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode left,
        ExecutableNode right) implements ExecutableNode {

    public BinaryNullCoalesceExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object value = left.execute(scope);
        return value != null ? value : right.execute(scope);
    }
}
