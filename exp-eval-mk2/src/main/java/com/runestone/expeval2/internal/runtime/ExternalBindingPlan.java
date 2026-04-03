package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.types.ResolvedType;

record ExternalBindingPlan(SymbolRef symbolRef, ResolvedType declaredType, boolean overridable) {

    /**
     * Delegates to {@code symbolRef().index()} to keep a single authoritative source for the
     * array position of this external symbol. The index is assigned during compilation by
     * {@code ExecutionPlanBuilder.assignIndices()} and must not diverge from the position used
     * in the defaults array built by {@code seedDefaults()}.
     */
    int index() {
        return symbolRef.index();
    }
}