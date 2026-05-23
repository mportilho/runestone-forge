package com.runestone.expeval.internal.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SymbolIndexAllocator {

    private SymbolIndexAllocator() {
    }

    /**
     * Assigns stable, zero-based integer indices to all internal and external symbols.
     */
    public static void assignIndices(SemanticModel model) {
        Objects.requireNonNull(model, "model must not be null");
        int internalIndex = 0;
        List<String> sortedInternalNames = new ArrayList<>(model.internalSymbolsByName().keySet());
        Collections.sort(sortedInternalNames);
        for (String name : sortedInternalNames) {
            model.internalSymbolsByName().get(name).setIndex(internalIndex++);
        }

        int externalIndex = 0;
        List<String> sortedExternalNames = new ArrayList<>(model.externalSymbolsByName().keySet());
        Collections.sort(sortedExternalNames);
        for (String name : sortedExternalNames) {
            model.externalSymbolsByName().get(name).setIndex(externalIndex++);
        }
    }
}
