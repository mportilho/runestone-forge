package com.runestone.expeval_mk3.internal.semantics;

import java.util.HashSet;
import java.util.List;

public record FrameLayout(List<SymbolBinding> externalBindings, int assignmentExternalBindingCount, int frameSize) {

    public FrameLayout {
        externalBindings = List.copyOf(externalBindings);
        if (assignmentExternalBindingCount < 0 || assignmentExternalBindingCount > externalBindings.size()) {
            throw new IllegalArgumentException("assignmentExternalBindingCount must be within externalBindings");
        }
        if (frameSize < externalBindings.size()) {
            throw new IllegalArgumentException("frameSize must cover external bindings");
        }
        HashSet<Integer> occupiedSlots = new HashSet<>();
        for (SymbolBinding binding : externalBindings) {
            if (!binding.external()) {
                throw new IllegalArgumentException("externalBindings must contain only external symbols");
            }
            if (binding.frameSlot() >= frameSize) {
                throw new IllegalArgumentException("external binding slot must be within the frame");
            }
            if (!occupiedSlots.add(binding.frameSlot())) {
                throw new IllegalArgumentException("external binding slots must be unique");
            }
        }
    }
}
