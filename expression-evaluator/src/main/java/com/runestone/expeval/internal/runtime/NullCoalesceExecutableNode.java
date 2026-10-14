package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;

import java.util.List;
import java.util.Objects;

/** Evaluates operands left to right, stopping at the first non-null value. */
public record NullCoalesceExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<ExecutableNode> operands) implements ExecutableNode {

    public NullCoalesceExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        operands = List.copyOf(Objects.requireNonNull(operands, "operands"));
    }

    @Override
    public Object execute(ExecutionScope scope) {
        for (ExecutableNode operand : operands) {
            Object value = operand.execute(scope);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
