package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;

import java.util.List;
import java.util.Objects;

/** A prepared or folded value, with any source calculations collapsed into it. */
public sealed class ConstantExecutableNode implements ExecutableNode permits StaticCalculationConstantExecutableNode {

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

    /** Values retained by this constant for cache-admission accounting. */
    public List<Object> retainedValues() {
        return List.of(value);
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return value;
    }

    StaticCalculationGroup calculationGroup() {
        return StaticCalculationGroup.EMPTY;
    }
}
