package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.types.ResolvedType;

record ExternalBindingPlan(SymbolRef symbolRef, ResolvedType declaredType, boolean overridable) {
}