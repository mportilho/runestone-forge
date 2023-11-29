package com.runestone.converters.impl;

public record ConverterPairKey(Class<?> sourceType, Class<?> targetType) {

    public int hashCode() {
        return this.sourceType.hashCode() * 31 + this.targetType.hashCode();
    }
}
