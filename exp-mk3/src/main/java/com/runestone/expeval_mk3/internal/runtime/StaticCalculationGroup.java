package com.runestone.expeval_mk3.internal.runtime;

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

    void capture(ExecutionScope scope) {
        scope.captureCalculations(ordinals, replaySlots, values);
    }
}
