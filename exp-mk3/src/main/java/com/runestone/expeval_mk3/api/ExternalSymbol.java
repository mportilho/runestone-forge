package com.runestone.expeval_mk3.api;

import java.util.Objects;
import java.util.Optional;

/**
 * Declaration of a value supplied from outside the expression.
 */
public final class ExternalSymbol {

    private final String name;
    private final ExpressionType type;
    private final ExternalSymbolDefault defaultValue;

    private ExternalSymbol(String name, ExpressionType type, ExternalSymbolDefault defaultValue) {
        this.name = ExternalSymbolNames.validate(name);
        this.type = Objects.requireNonNull(type, "type");
        this.defaultValue = defaultValue;
    }

    public static ExternalSymbol unknown(String name) {
        return new ExternalSymbol(name, UnknownType.INSTANCE, null);
    }

    public static ExternalSymbol declared(String name, ExpressionType type) {
        return new ExternalSymbol(name, type, null);
    }

    public static ExternalSymbol withDefault(String name, ExpressionType type, Object value) {
        String validatedName = ExternalSymbolNames.validate(name);
        ExpressionType requestedType = Objects.requireNonNull(type, "type");
        ExpressionType effectiveType = requestedType == UnknownType.INSTANCE
                ? ExternalSymbolDefaults.inferType(validatedName, value)
                : requestedType;
        Object canonicalValue = ExternalSymbolDefaults.canonicalize(validatedName, effectiveType, value);
        return new ExternalSymbol(validatedName, effectiveType, new ExternalSymbolDefault(effectiveType, canonicalValue));
    }

    public String name() {
        return name;
    }

    public ExpressionType type() {
        return type;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public Optional<ExternalSymbolDefault> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExternalSymbol that)) {
            return false;
        }
        return name.equals(that.name)
                && type.equals(that.type)
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue);
    }

    @Override
    public String toString() {
        return "ExternalSymbol[name=" + name + ", type=" + type + ", hasDefaultValue=" + hasDefaultValue() + ']';
    }
}
