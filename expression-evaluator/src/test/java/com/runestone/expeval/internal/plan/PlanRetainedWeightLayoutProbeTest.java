package com.runestone.expeval.internal.plan;

import com.runestone.expeval.internal.runtime.ConstantExecutableNode;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import static org.assertj.core.api.Assertions.assertThat;

class PlanRetainedWeightLayoutProbeTest {

    @Test
    void planAndExecutableNodeUnitsAreCalibratedAgainstTheirJolLayouts() {
        assertThat(PlanRetainedWeight.PLAN_UNITS)
                .isGreaterThanOrEqualTo(Math.toIntExact(ClassLayout.parseClass(ExecutionPlan.class).instanceSize()));
        assertThat(PlanRetainedWeight.EXECUTABLE_NODE_UNITS)
                .isGreaterThanOrEqualTo(Math.toIntExact(ClassLayout.parseClass(ConstantExecutableNode.class).instanceSize()));
    }
}
