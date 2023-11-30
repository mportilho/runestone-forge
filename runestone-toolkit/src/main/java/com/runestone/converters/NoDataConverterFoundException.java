package com.runestone.converters;

import java.util.Objects;

public class NoDataConverterFoundException extends RuntimeException {

    private final Class<?> sourceType;
    private final Class<?> targetType;

    public NoDataConverterFoundException(Class<?> sourceType, Class<?> targetType) {
        super(String.format("No converter found for source %s and target %s",
                sourceType.getCanonicalName(), targetType.getCanonicalName()));
        this.sourceType = Objects.requireNonNull(sourceType);
        this.targetType = Objects.requireNonNull(targetType);
    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }
}
