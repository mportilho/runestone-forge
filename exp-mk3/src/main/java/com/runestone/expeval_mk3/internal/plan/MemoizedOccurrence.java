package com.runestone.expeval_mk3.internal.plan;

import java.util.Objects;

/** Frame and provenance metadata for one source occurrence in a memoized structural group. */
record MemoizedOccurrence(int memoSlot, int[] calculationSlots, int[] replaySlots) {

    MemoizedOccurrence {
        Objects.requireNonNull(calculationSlots, "calculationSlots");
        Objects.requireNonNull(replaySlots, "replaySlots");
        if (calculationSlots.length != replaySlots.length) {
            throw new IllegalArgumentException("calculation and replay slots must have equal lengths");
        }
    }
}
