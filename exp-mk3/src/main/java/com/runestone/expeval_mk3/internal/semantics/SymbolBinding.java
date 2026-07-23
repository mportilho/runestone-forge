package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.Objects;

public record SymbolBinding(ExternalSymbol symbol, int frameSlot, RuntimeNullability runtimeNullability) {

    public SymbolBinding {
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(runtimeNullability, "runtimeNullability");
        if (frameSlot < 0) {
            throw new IllegalArgumentException("frameSlot must not be negative");
        }
    }
}
