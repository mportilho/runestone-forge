package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.semantic.SymbolRef;
import com.runestone.expeval.types.ResolvedType;

public record ExternalBindingPlan(SymbolRef symbolRef, ResolvedType declaredType, boolean overridable) {

    /**
     * Delegates to {@code symbolRef().index()} to keep a single authoritative source for the
     * array position of this external symbol. The index is assigned during compilation by
     * {@code SymbolIndexAllocator.assignIndices()} and must not diverge from the position used
     * in the defaults array built by {@code ExternalBindingPlanner.seedDefaults()}.
     */
    public int index() {
        return symbolRef.index();
    }
}
