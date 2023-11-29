package com.runestone.converters;

public interface DataConversionService {

    /**
     * Indicates if this conversion service can convert a sourceType to a targetType
     *
     * @param sourceType The source's class to be converted from
     * @param targetType The source's target class to be converted to
     * @return an indication if there's a converter for the provided types
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /**
     * Converts an object to a target type
     *
     * @param source     The value to be converted
     * @param targetType Target types for conversion
     * @param <S>        Source Type
     * @param <T>        Target type
     * @return the converted value
     */
    <S, T> T convert(S source, Class<T> targetType);
}
