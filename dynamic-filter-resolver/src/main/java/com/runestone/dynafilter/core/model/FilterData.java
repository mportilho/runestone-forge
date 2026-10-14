/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.exceptions.MultipleFilterDataValuesException;
import com.runestone.dynafilter.core.operation.FilterOperation;

import java.util.List;

import static com.runestone.dynafilter.helpers.StringHelper.formatPath;

/**
 * Represents the data that will be used to build a filter.
 *
 * <p>
 * Each parameter has a corresponding value. The values are stored in a two dimensional array. The first array
 * position is the parameter's position, corresponding to the 'parameters' attribute,  and the second is the values it has.
 * </p>
 *
 * @param path        the paths to the attributes to be filtered
 * @param parameters  the parameters that will be used to build the filter
 * @param targetType  the target type of the attribute to be filtered
 * @param operation   the operation that will be used to build the filter
 * @param negate      if the filter's result should be negated
 * @param values      The values used to construct and apply the filter to the attribute
 * @param modifiers   the modifiers that will be used to build the filter
 * @param description the user readable description of the filter
 */
public record FilterData(
        String[] path,
        String[] parameters,
        Class<?> targetType,
        @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
        boolean negate,
        Object[] values,
        List<Class<? extends FilterModifier>> modifiers,
        String description
) {

    public FilterData {
        if (parameters == null || parameters.length == 0) {
            throw new IllegalArgumentException("On path '%s', parameters cannot be null or empty".formatted(formatPath(path)));
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("On path '%s', values cannot be null or empty".formatted(formatPath(path)));
        }
        if (parameters.length != values.length) {
            throw new IllegalArgumentException("On path '%s', parameters and values must have the same length, as each parameter must have a corresponding value".formatted(formatPath(path)));
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
                                @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation, Object[] values) {
        return new FilterData(new String[]{path}, parameters, targetType, operation, false, values, null, null);
    }

    /**
     * Creates a filter with one path, one parameter and one outer value slot.
     * The parameter name is metadata; it does not trigger request-value lookup.
     * A null or array value remains one slot, and the value is shared without cloning.
     * Conversion and operation-specific validation remain in the resolution flow.
     * Negation is disabled, and modifiers and description are absent.
     *
     * @param path       the non-blank path to the attribute to be filtered
     * @param parameter  the non-blank parameter name
     * @param targetType the explicit target attribute type, not inferred from the value
     * @param operation  the operation that will be used to build the filter
     * @param value      the single value, possibly null or an array
     * @return a new instance of {@link FilterData}
     * @throws IllegalArgumentException if the path or parameter name is null or blank
     */
    public static FilterData single(String path, String parameter, Class<?> targetType,
                                    @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation, Object value) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be null or blank");
        }
        if (parameter == null || parameter.isBlank()) {
            throw new IllegalArgumentException("Parameter cannot be null or blank");
        }
        return of(path, new String[]{parameter}, targetType, operation, new Object[]{value});
    }

    /**
     * Creates a single-slot filter using the path as its parameter name.
     * This has the same value-sharing, conversion and default semantics as
     * {@link #single(String, String, Class, Class, Object)}.
     *
     * @param path       the non-blank path to the attribute and parameter name
     * @param targetType the explicit target attribute type
     * @param operation  the operation that will be used to build the filter
     * @param value      the single value, possibly null or an array
     * @return a new instance of {@link FilterData}
     * @throws IllegalArgumentException if the path is null or blank
     */
    public static FilterData single(String path, Class<?> targetType,
                                    @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation, Object value) {
        return single(path, path, targetType, operation, value);
    }

    /**
     * @return the parameter's value if there's one provided from the first array
     * position. Returns null if none is found.
     * @throws MultipleFilterDataValuesException if there's more than one value in the first array position or if there's more than one array position
     */
    @SuppressWarnings("unchecked")
    public <R> R findOneValue() {
        if (values.length > 1) {
            throw new MultipleFilterDataValuesException(String.format("Multiple values found while fetching a single one for path [%s]", formatPath(path)));
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
            throw new ArrayIndexOutOfBoundsException(String.format("Accessing nonexistent index [%s] for path [%s]", i, formatPath(path)));
        }
        return values[i];
    }

    public boolean hasModifier(Class<? extends FilterModifier> modifier) {
        return modifiers != null && modifiers.contains(modifier);
    }

}
