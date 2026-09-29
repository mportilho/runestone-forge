package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;

import java.util.Objects;

/** A folded constant that replays the static calculation provenance collapsed into its value. */
final class StaticCalculationConstantExecutableNode extends ConstantExecutableNode {

    private final StaticCalculationGroup calculationGroup;

    StaticCalculationConstantExecutableNode(
            NodeId id, SourceSpan sourceSpan, Object value, StaticCalculationGroup calculationGroup) {
        super(id, sourceSpan, value);
        this.calculationGroup = Objects.requireNonNull(calculationGroup, "calculationGroup");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        calculationGroup.capture(scope);
        return value();
    }

    @Override
    public java.util.List<Object> retainedValues() {
        return calculationGroup.retainedValues(value());
    }

    void visitRetainedPayload(ExecutableNodeRetainedPayload.Visitor visitor) {
        visitor.constant(this);
        calculationGroup.visitRetainedPayload(visitor);
    }

    @Override
    StaticCalculationGroup calculationGroup() {
        return calculationGroup;
    }
}
