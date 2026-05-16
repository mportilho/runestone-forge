package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.modifier.FilterModifier;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.exception.MultipleFilterDataValuesException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record FilterData(
        String path,
        String[] parameters,
        Class<?> targetType,
        Class<? extends DefinedFilterOperation> operation,
        boolean negate,
        Object[] values,
        List<Class<? extends FilterModifier>> modifiers,
        String description
) {

    public FilterData {
        parameters = copyRequiredArray(parameters, "parameters");
        values = copyRequiredArray(values, "values");
        if (parameters.length != values.length) {
            throw new IllegalArgumentException("parameters and values must have the same size");
        }
        modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }

    public String[] parameters() {
        return parameters.clone();
    }

    public Object[] values() {
        return values.clone();
    }

    public boolean hasModifier(Class<? extends FilterModifier> modifier) {
        return modifier != null && modifiers.contains(modifier);
    }

    public Object findOneValue() {
        if (values.length != 1) {
            throw new MultipleFilterDataValuesException("FilterData must have exactly one value");
        }
        return values[0];
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FilterData that)) {
            return false;
        }
        return negate == that.negate
                && Objects.equals(path, that.path)
                && Arrays.equals(parameters, that.parameters)
                && Objects.equals(targetType, that.targetType)
                && Objects.equals(operation, that.operation)
                && Arrays.equals(values, that.values)
                && Objects.equals(modifiers, that.modifiers)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(path, targetType, operation, negate, modifiers, description);
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        return "FilterData["
                + "path=" + path
                + ", parameters=" + Arrays.toString(parameters)
                + ", targetType=" + targetType
                + ", operation=" + operation
                + ", negate=" + negate
                + ", values=" + Arrays.toString(values)
                + ", modifiers=" + modifiers
                + ", description=" + description
                + ']';
    }

    private static <T> T[] copyRequiredArray(T[] array, String name) {
        Objects.requireNonNull(array, name + " must not be null");
        if (array.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return array.clone();
    }
}
