package com.runestone.expeval_mk3.internal.semantics;

import java.util.List;
import java.util.Objects;

public record FrameLayout(List<FrameSlot> slots) {

    public FrameLayout {
        Objects.requireNonNull(slots, "slots");
        slots = List.copyOf(slots);
        for (int index = 0; index < slots.size(); index++) {
            if (slots.get(index).index() != index) {
                throw new IllegalArgumentException("frame slot indexes must be contiguous from zero");
            }
        }
    }

    public int frameSize() {
        return slots.size();
    }
}
