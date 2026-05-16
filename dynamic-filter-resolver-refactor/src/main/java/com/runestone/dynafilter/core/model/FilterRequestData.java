package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.modifier.FilterModifier;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record FilterRequestData(
        String path,
        String[] parameters,
        Class<?> targetType,
        Class<? extends DefinedFilterOperation> operation,
        String negate,
        Object[] defaultValues,
        Object[] constantValues,
        String format,
        boolean required,
        List<Class<? extends FilterModifier>> modifiers,
        String description
) {

    public FilterRequestData {
        path = Objects.requireNonNull(path, "path must not be null");
        parameters = copyRequiredArray(parameters, "parameters");
        targetType = Objects.requireNonNull(targetType, "targetType must not be null");
        operation = Objects.requireNonNull(operation, "operation must not be null");
        defaultValues = copyOptionalArray(defaultValues);
        constantValues = copyOptionalArray(constantValues);
        validateOptionalValues(parameters, defaultValues, "defaultValues");
        validateOptionalValues(parameters, constantValues, "constantValues");
        modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }

    public static FilterRequestData from(Filter filter) {
        Objects.requireNonNull(filter, "filter must not be null");
        return new FilterRequestData(
                filter.path(),
                filter.parameters(),
                filter.targetType(),
                filter.operation(),
                filter.negate(),
                filter.defaultValues(),
                filter.constantValues(),
                filter.format(),
                filter.required(),
                List.of(filter.modifiers()),
                filter.description()
        );
    }

    public String[] parameters() {
        return parameters.clone();
    }

    public Object[] defaultValues() {
        return defaultValues.clone();
    }

    public Object[] constantValues() {
        return constantValues.clone();
    }

    public boolean hasConstantValues() {
        return constantValues.length > 0;
    }

    public boolean hasDefaultValues() {
        return defaultValues.length > 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FilterRequestData that)) {
            return false;
        }
        return required == that.required
                && Objects.equals(path, that.path)
                && Arrays.equals(parameters, that.parameters)
                && Objects.equals(targetType, that.targetType)
                && Objects.equals(operation, that.operation)
                && Objects.equals(negate, that.negate)
                && Arrays.equals(defaultValues, that.defaultValues)
                && Arrays.equals(constantValues, that.constantValues)
                && Objects.equals(format, that.format)
                && Objects.equals(modifiers, that.modifiers)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(path, targetType, operation, negate, format, required, modifiers, description);
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + Arrays.hashCode(defaultValues);
        result = 31 * result + Arrays.hashCode(constantValues);
        return result;
    }

    @Override
    public String toString() {
        return "FilterRequestData["
                + "path=" + path
                + ", parameters=" + Arrays.toString(parameters)
                + ", targetType=" + targetType
                + ", operation=" + operation
                + ", negate=" + negate
                + ", defaultValues=" + Arrays.toString(defaultValues)
                + ", constantValues=" + Arrays.toString(constantValues)
                + ", format=" + format
                + ", required=" + required
                + ", modifiers=" + modifiers
                + ", description=" + description
                + ']';
    }

    private static String[] copyRequiredArray(String[] array, String name) {
        Objects.requireNonNull(array, name + " must not be null");
        if (array.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return array.clone();
    }

    private static Object[] copyOptionalArray(Object[] array) {
        return array == null ? new Object[0] : array.clone();
    }

    private static void validateOptionalValues(String[] parameters, Object[] values, String name) {
        if (values.length > 0 && values.length != parameters.length) {
            throw new IllegalArgumentException(name + " must be empty or have the same size as parameters");
        }
    }
}
