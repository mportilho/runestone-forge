package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.Objects;

public final class SymbolBinding {

    private final String name;
    private final ExpressionType type;
    private final ExternalSymbol externalSymbol;
    private final int frameSlot;
    private final RuntimeNullability runtimeNullability;

    private SymbolBinding(
            String name,
            ExpressionType type,
            ExternalSymbol externalSymbol,
            int frameSlot,
            RuntimeNullability runtimeNullability) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.externalSymbol = externalSymbol;
        this.runtimeNullability = Objects.requireNonNull(runtimeNullability, "runtimeNullability");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (frameSlot < 0) {
            throw new IllegalArgumentException("frameSlot must not be negative");
        }
        this.frameSlot = frameSlot;
    }

    public static SymbolBinding external(ExternalSymbol symbol, int frameSlot) {
        Objects.requireNonNull(symbol, "symbol");
        return new SymbolBinding(
                symbol.name(),
                symbol.type(),
                symbol,
                frameSlot,
                RuntimeNullability.NEVER_NULL);
    }

    public static SymbolBinding internal(String name, ExpressionType type, int frameSlot) {
        return new SymbolBinding(name, type, null, frameSlot, RuntimeNullability.NEVER_NULL);
    }

    public static SymbolBinding internal(
            String name,
            ExpressionType type,
            int frameSlot,
            RuntimeNullability runtimeNullability) {
        return new SymbolBinding(name, type, null, frameSlot, runtimeNullability);
    }

    public String name() {
        return name;
    }

    public ExpressionType type() {
        return type;
    }

    public int frameSlot() {
        return frameSlot;
    }

    public RuntimeNullability runtimeNullability() {
        return runtimeNullability;
    }

    public boolean external() {
        return externalSymbol != null;
    }

    public ExternalSymbol requireExternalSymbol() {
        if (externalSymbol == null) {
            throw new IllegalStateException("symbol binding is not external: " + name);
        }
        return externalSymbol;
    }
}
