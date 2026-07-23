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
    private final int maxMaterializedSize;

    private ExternalSymbol(
            String name,
            ExpressionType type,
            ExternalSymbolDefault defaultValue,
            ExternalSymbolOverwritePolicy overwritePolicy,
            int maxMaterializedSize) {
        this.name = ExternalSymbolNames.validate(name);
        this.type = Objects.requireNonNull(type, "type");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.overwritePolicy = Objects.requireNonNull(overwritePolicy, "overwritePolicy");
        this.maxMaterializedSize = maxMaterializedSize;
    }

    public static ExternalSymbol withDefault(
            String name,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy) {
        String validatedName = ExternalSymbolNames.validate(name);
        return withInferredDefault(
                validatedName,
                value,
                overwritePolicy,
                BoundaryCoercion.standard(),
                BoundaryCoercion.DEFAULT_MAX_MATERIALIZED_SIZE);
    }

    public static ExternalSymbol withDefault(
            String name,
            ExpressionType type,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy) {
        return withDefault(
                name,
                type,
                value,
                overwritePolicy,
                BoundaryCoercion.standard(),
                BoundaryCoercion.DEFAULT_MAX_MATERIALIZED_SIZE);
    }

    static ExternalSymbol withInferredDefault(
            String name,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        String validatedName = ExternalSymbolNames.validate(name);
        ExternalSymbolDefaults.PreparedDefault preparedDefault = ExternalSymbolDefaults.prepare(
                validatedName, value, boundaryCoercion, maxMaterializedSize);
        return create(
                validatedName,
                preparedDefault.type(),
                preparedDefault.value(),
                overwritePolicy,
                maxMaterializedSize);
    }

    static ExternalSymbol withDefault(
            String name,
            ExpressionType type,
            Object value,
            ExternalSymbolOverwritePolicy overwritePolicy,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        String validatedName = ExternalSymbolNames.validate(name);
        ExpressionType effectiveType = Objects.requireNonNull(type, "type");
        ExternalSymbolOverwritePolicy effectiveOverwritePolicy = Objects.requireNonNull(
                overwritePolicy,
                "overwritePolicy");
        Object canonicalValue = boundaryCoercion.convertDefault(
                validatedName, value, effectiveType, maxMaterializedSize);
        return create(validatedName, effectiveType, canonicalValue, effectiveOverwritePolicy, maxMaterializedSize);
    }

    private static ExternalSymbol create(
            String name,
            ExpressionType type,
            Object canonicalValue,
            ExternalSymbolOverwritePolicy overwritePolicy,
            int maxMaterializedSize) {
        return new ExternalSymbol(
                name,
                type,
                new ExternalSymbolDefault(type, canonicalValue),
                overwritePolicy,
                maxMaterializedSize);
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
        return boundaryCoercion.convertOverride(name, value, type, maxMaterializedSize);
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
                && overwritePolicy == that.overwritePolicy
                && maxMaterializedSize == that.maxMaterializedSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, overwritePolicy, maxMaterializedSize);
    }

    @Override
    public String toString() {
        return "ExternalSymbol[name=" + name + ", type=" + type + ", overwritePolicy=" + overwritePolicy + ']';
    }
}
