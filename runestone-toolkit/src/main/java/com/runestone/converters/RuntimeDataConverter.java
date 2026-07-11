package com.runestone.converters;

public interface RuntimeDataConverter<S, T> {

    Class<S> sourceType();

    Class<T> targetType();

    T convert(S source, ConversionContext context);
}
