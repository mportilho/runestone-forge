package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/** A prepared or folded value, with any source calculations collapsed into it. */
public sealed class ConstantExecutableNode implements ExecutableNode permits CapturedConstantExecutableNode {

    private final NodeId id;
    private final SourceSpan sourceSpan;
    private final Object value;

    public ConstantExecutableNode(NodeId id, SourceSpan sourceSpan, Object value) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public NodeId id() {
        return id;
    }

    @Override
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    public Object value() {
        return value;
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return value;
    }

    StaticCalculationGroup calculationGroup() {
        return StaticCalculationGroup.EMPTY;
    }
}
