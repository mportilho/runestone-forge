package com.runestone.expeval_mk3.internal.semantics;

import java.util.List;
import java.util.Objects;

public record ResolvedSymbolSets(
        List<ResolvedSymbol> internalSymbols,
        List<ResolvedSymbol> externalSymbols,
        List<ResolvedSymbol> currentItemSymbols) {

    public ResolvedSymbolSets {
        Objects.requireNonNull(internalSymbols, "internalSymbols");
        internalSymbols = List.copyOf(internalSymbols);
        Objects.requireNonNull(externalSymbols, "externalSymbols");
        externalSymbols = List.copyOf(externalSymbols);
        Objects.requireNonNull(currentItemSymbols, "currentItemSymbols");
        currentItemSymbols = List.copyOf(currentItemSymbols);
    }

    public static ResolvedSymbolSets empty() {
        return new ResolvedSymbolSets(List.of(), List.of(), List.of());
    }
}
