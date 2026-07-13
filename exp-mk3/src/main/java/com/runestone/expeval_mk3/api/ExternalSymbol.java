package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Declaration of a value supplied from outside the expression.
 */
public final class ExternalSymbol {

    private final String name;
    private final ExpressionType type;
    private final ExternalSymbolDefault defaultValue;
    private final ExternalSymbolOverwritePolicy overwritePolicy;

    private ExternalSymbol(
            String name,
            ExpressionType type,
            ExternalSymbolDefault defaultValue,
            ExternalSymbolOverwritePolicy overwritePolicy) {
        this.name = ExternalSymbolNames.validate(name);
        this.type = Objects.requireNonNull(type, "type");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.overwritePolicy = Objects.requireNonNull(overwritePolicy, "overwritePolicy");
    }

    public static ExternalSymbol withDefault(
            String name,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy) {
        String validatedName = ExternalSymbolNames.validate(name);
        ExpressionType inferredType = ExternalSymbolDefaults.inferType(validatedName, value);
        return withDefault(validatedName, inferredType, value, overwritePolicy);
    }

    public static ExternalSymbol withDefault(
            String name,
            ExpressionType type,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy) {
        return withDefault(name, type, value, overwritePolicy, BoundaryCoercion.standard());
    }

    static ExternalSymbol withDefault(
            String name,
            ExpressionType type,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy,
            BoundaryCoercion boundaryCoercion) {
        String validatedName = ExternalSymbolNames.validate(name);
        ExpressionType effectiveType = Objects.requireNonNull(type, "type");
        ExternalSymbolOverwritePolicy effectiveOverwritePolicy = Objects.requireNonNull(
                overwritePolicy,
                "overwritePolicy");
        Object canonicalValue = ExternalSymbolDefaults.canonicalize(
                validatedName,
                effectiveType,
                value,
                boundaryCoercion);
        return new ExternalSymbol(
                validatedName,
                effectiveType,
                new ExternalSymbolDefault(effectiveType, canonicalValue),
                effectiveOverwritePolicy);
    }

    public String name() {
        return name;
    }

    public ExpressionType type() {
        return type;
    }

    public ExternalSymbolDefault defaultValue() {
        return defaultValue;
    }

    public ExternalSymbolOverwritePolicy overwritePolicy() {
        return overwritePolicy;
    }

    public Object coerceOverride(Object value, BoundaryCoercion boundaryCoercion) {
        Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        if (overwritePolicy != ExternalSymbolOverwritePolicy.OVERRIDABLE) {
            throw new IllegalStateException("external symbol '" + name + "' is not overridable");
        }
        if (value == null) {
            throw new IllegalArgumentException("external symbol '" + name + "' override must not be null");
        }
        return boundaryCoercion.convertOverride(name, value, type);
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
                && defaultValue.equals(that.defaultValue)
                && overwritePolicy == that.overwritePolicy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, overwritePolicy);
    }

    @Override
    public String toString() {
        return "ExternalSymbol[name=" + name + ", type=" + type + ", overwritePolicy=" + overwritePolicy + ']';
    }
}
