package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

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
    StaticCalculationGroup calculationGroup() {
        return calculationGroup;
    }
}
