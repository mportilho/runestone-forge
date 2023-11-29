package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.exceptions.MultipleFilterDataValuesException;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;

import java.util.List;

/**
 * Represents the data that will be used to build a filter.
 *
 * <p>
 * Each parameter has a corresponding value. The values are stored in a two dimensional array. The first array
 * position is the parameter's position, corresponding to the 'parameters' attribute,  and the second is the values it has.
 * </p>
 *
 * @param path        the path to the attribute to be filtered
 * @param parameters  the parameters that will be used to build the filter
 * @param targetType  the target type of the attribute to be filtered
 * @param operation   the operation that will be used to build the filter
 * @param negate      if the filter's result should be negated
 * @param values      the values that will be used to build the filter and that will be used to filter the attribute
 * @param modifiers   the modifiers that will be used to build the filter
 * @param description the user readable description of the filter
 */
public record FilterData(
        String path,
        String[] parameters,
        Class<?> targetType,
        Class<? super DefinedFilterOperation> operation,
        boolean negate,
        Object[] values,
        List<Class<? extends FilterModifier>> modifiers,
        String description
) {

    public FilterData {
        if (parameters == null || parameters.length == 0) {
            throw new IllegalArgumentException("Parameters cannot be null or empty");
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values cannot be null or empty");
        }
        if (parameters.length != values.length) {
            throw new IllegalArgumentException("Parameters and values must have the same length, as each parameter must have a corresponding value");
        }
    }

    /**
     * Creates a new instance of {@link FilterData} with the provided parameters
     *
     * @param path       the path to the attribute to be filtered
     * @param parameters the parameters that will be used to build the filter
     * @param targetType the target type of the attribute to be filtered
     * @param operation  the operation that will be used to build the filter
     * @param values     the values that will be used to build the filter and that will be used to filter the attribute
     * @return a new instance of {@link FilterData}
     */
    public static FilterData of(String path, String[] parameters, Class<?> targetType,
                                Class<? super DefinedFilterOperation> operation, Object[] values) {
        return new FilterData(path, parameters, targetType, operation, false, values, null, null);
    }

    /**
     * @return the parameter's value if there's one provided from the first array
     * position. Returns null if none is found.
     * @throws MultipleFilterDataValuesException if there's more than one value in the first array position or if there's more than one array position
     */
    @SuppressWarnings("unchecked")
    public <R> R findOneValue() {
        if (values.length > 1) {
            throw new MultipleFilterDataValuesException(String.format("Multiple values found while fetching a single one for path [%s]", path));
        }
        return (R) values[0];
    }

    /**
     * @param i the index of the value to be fetched
     * @return the value on the provided index
     * @throws ArrayIndexOutOfBoundsException    if the provided index is greater than the number of values
     * @throws MultipleFilterDataValuesException if there's more than one value in the provided index
     */
    public Object findValueOnIndex(int i) {
        if (i >= values.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("Accessing nonexistent index [%s] for path [%s]", i, path));
        }
        return values[i];
    }

    public boolean hasModifier(Class<? extends FilterModifier> modifier) {
        return modifiers != null && modifiers.contains(modifier);
    }

}