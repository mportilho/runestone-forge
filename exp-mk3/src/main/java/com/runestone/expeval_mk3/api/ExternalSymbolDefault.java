package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Present non-null default value for a Simbolo Externo.
 */
public final class ExternalSymbolDefault {

    private final ExpressionType type;
    private final Object value;

    ExternalSymbolDefault(ExpressionType type, Object value) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = Objects.requireNonNull(value, "value");
    }

    public ExpressionType type() {
        return type;
    }

    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExternalSymbolDefault that)) {
            return false;
        }
        return type.equals(that.type) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "ExternalSymbolDefault[type=" + type + ", hasValue=true]";
    }
}
