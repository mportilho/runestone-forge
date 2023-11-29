package com.runestone.converters.impl;

import com.runestone.converters.DataConversionService;

public class DelegateDataConversionService implements DataConversionService {

    private final DefaultDataConversionService defaultDataConversionService;
    private final DataConversionService delegate;

    public DelegateDataConversionService(DataConversionService delegate) {
        this.defaultDataConversionService = new DefaultDataConversionService(false);
        this.delegate = delegate;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return defaultDataConversionService.canConvert(sourceType, targetType) || delegate.canConvert(sourceType, targetType);
    }

    @Override
    public <S, T> T convert(S source, Class<T> targetType) {
        T convertedValue = defaultDataConversionService.convert(source, targetType);
        if (convertedValue == null) {
            convertedValue = delegate.convert(source, targetType);
        }
        return convertedValue;
    }

}
