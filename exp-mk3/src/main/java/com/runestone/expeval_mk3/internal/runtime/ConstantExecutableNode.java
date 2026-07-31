package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/** A literal's prepared value, already resolved by semantics; execution just returns it. */
public record ConstantExecutableNode(NodeId id, SourceSpan sourceSpan, Object value) implements ExecutableNode {

    public ConstantExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return value;
    }
}
