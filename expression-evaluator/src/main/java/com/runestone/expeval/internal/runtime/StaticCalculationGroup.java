package com.runestone.expeval.internal.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable folded calculation values replayed when their replacement constant is reached. */
final class StaticCalculationGroup {

    static final StaticCalculationGroup EMPTY = new StaticCalculationGroup(new int[0], new int[0][], new Object[0]);

    private final int[] ordinals;
    private final int[][] replaySlots;
    private final Object[] values;

    StaticCalculationGroup(int[] ordinals, int[][] replaySlots, Object[] values) {
        this.ordinals = Objects.requireNonNull(ordinals, "ordinals");
        this.replaySlots = Objects.requireNonNull(replaySlots, "replaySlots");
        this.values = Objects.requireNonNull(values, "values");
        if (ordinals.length != replaySlots.length || ordinals.length != values.length) {
            throw new IllegalArgumentException("calculation ordinals, replay slots, and values must have equal lengths");
        }
    }

    boolean isEmpty() {
        return ordinals.length == 0;
    }

    List<Object> retainedValues(Object primaryValue) {
        List<Object> retained = new ArrayList<>(values.length + 1);
        retained.add(primaryValue);
        for (Object value : values) {
            retained.add(value);
        }
        return Collections.unmodifiableList(retained);
    }

    void visitRetainedPayload(ExecutableNodeRetainedPayload.Visitor visitor) {
        visitor.value(ordinals);
        visitor.value(replaySlots);
    }

    void capture(ExecutionScope scope) {
        scope.captureCalculations(ordinals, replaySlots, values);
    }
}
