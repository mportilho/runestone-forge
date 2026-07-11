package com.runestone.converters;

public interface RuntimeDataConversionService {

    ConversionContext conversionContext();

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    <S, T> T convert(S source, Class<T> targetType);
}
