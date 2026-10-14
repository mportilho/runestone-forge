package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.RuntimeDataConverter;

abstract class StringRuntimeConverter<T> implements RuntimeDataConverter<String, T> {

    private final Class<T> targetType;

    @SuppressWarnings("unchecked")
    StringRuntimeConverter(Class<?> targetType) {
        this.targetType = (Class<T>) targetType;
    }

    @Override
    public final Class<String> sourceType() {
        return String.class;
    }

    @Override
    public final Class<T> targetType() {
        return targetType;
    }
}
