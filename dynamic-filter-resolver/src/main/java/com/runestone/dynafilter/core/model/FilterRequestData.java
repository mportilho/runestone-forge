package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;

import java.util.List;

/**
 * Represents the data that will be used to build a filter.
 *
 * @param path           the path to the attribute to be filtered
 * @param parameters     the parameters that will be requested to build the filter
 * @param targetType     the target type of the attribute to be filtered
 * @param operation      the operation that will be used to build the filter
 * @param negate         if the filter's result should be negated. Can be 'true, 'false' or be parsed by the Spring Expression Language
 * @param defaultValues  the defined default values that could be used to build the filter
 * @param constantValues the defined constant values that will be used to build the filter
 * @param format         the format pattern to assist data conversion for the corresponding path type
 * @param required       if the filter is required. Default is false
 * @param modifiers      the modifiers that will be used to build the filter
 * @param description    the user readable description of the filter
 */
public record FilterRequestData(
        String path,
        String[] parameters,
        Class<?> targetType,
        Class<? super DefinedFilterOperation> operation,
        String negate,
        Object[] defaultValues,
        Object[] constantValues,
        String format,
        boolean required,
        List<Class<? extends FilterModifier>> modifiers,
        String description

) {

    public static FilterRequestData of(Filter filter) {
        return new FilterRequestData(filter.path(), filter.parameters(), filter.targetType(), filter.operation(),
                filter.negate(), filter.defaultValues(), filter.constantValues(), filter.format(), filter.required(),
                List.of(filter.modifiers()), filter.description());
    }


}
