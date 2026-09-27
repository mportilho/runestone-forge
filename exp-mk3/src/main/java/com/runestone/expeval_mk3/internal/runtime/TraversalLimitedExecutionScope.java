package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.ExpressionResourceLimits;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.memory.CalculationRecorder;

import java.time.Clock;
import java.time.ZoneId;

/** SAFE scope variant used only by executions that can visit collection or map entries. */
public final class TraversalLimitedExecutionScope extends SafeExecutionScope {
    private final int maximum;
    private int remaining;

    public TraversalLimitedExecutionScope(Object[] frame, ZoneId zoneId, Clock clock,
                                          CalculationRecorder recorder, ExpressionResourceLimits limits) {
        super(frame, zoneId, clock, recorder, limits);
        maximum = limits.maxTraversalSteps();
        remaining = maximum;
    }

    @Override
    public void visitTraversalStep(SourceSpan span) {
        if (remaining == 0) {
            throw RuntimeFailures.traversalStepLimitExceeded(maximum, span);
        }
        remaining--;
    }

    @Override
    public boolean enforcesTraversalStepLimit() {
        return true;
    }
}
