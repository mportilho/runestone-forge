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

package com.runestone.converters.impl;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.DataConverter;
import com.runestone.converters.NoDataConverterFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultDataConversionService implements DataConversionService {

    private static final Map<ConverterPairKey, DataConverter<?, ?>> CONVERTERS = loadConverters();

    private final boolean throwIfNull;

    public DefaultDataConversionService() {
        this(true);
    }

    public DefaultDataConversionService(boolean throwIfNull) {
        this.throwIfNull = throwIfNull;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return CONVERTERS.containsKey(new ConverterPairKey(sourceType, targetType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "Target Type must be provided");
        if (source == null) {
            return null;
        } else if (source.getClass().equals(targetType)) {
            return (T) source;
        }

        DataConverter<S, T> dataConverter = (DataConverter<S, T>) CONVERTERS.get(new ConverterPairKey(source.getClass(), targetType));
        T convertedValue = dataConverter != null ? dataConverter.convert(source) : fallbackConversion(source, targetType);

        if (convertedValue == null && throwIfNull) {
            throw new NoDataConverterFoundException(source.getClass(), targetType);
        }
        return convertedValue;
    }

    @SuppressWarnings("unchecked")
    private <S, T> T fallbackConversion(S source, Class<T> targetType) {
        if (targetType.isInstance(source)) {
            return (T) source;
        } else if (targetType.isPrimitive()) {
            return convertPrimitiveTypes(source, targetType);
        }
        return convertEnum(source, targetType);
    }

    @SuppressWarnings("unchecked")
    private <S, T> T convertPrimitiveTypes(S source, Class<T> targetType) {
        if (source instanceof Number number) {
            return (T) switch (targetType.getName()) {
                case "int" -> number.intValue();
                case "long" -> number.longValue();
                case "float" -> number.floatValue();
                case "double" -> number.doubleValue();
                case "short" -> number.shortValue();
                case "byte" -> (T) Byte.valueOf(number.byteValue());
                default -> throw new IllegalStateException("Unexpected value: " + targetType.getName());
            };
        } else if (source instanceof Boolean bool) {
            if (targetType.equals(boolean.class)) {
                return (T) bool;
            }
        }
        return null;
    }

    private <S, T> T convertEnum(S source, Class<T> targetType) {
        if (targetType.isEnum()) {
            if (source instanceof String value) {
                for (T enumConstant : targetType.getEnumConstants()) {
                    if (((Enum<?>) enumConstant).name().equalsIgnoreCase(value)) {
                        return enumConstant;
                    }
                }
            } else if (source instanceof Number value) {
                for (T enumConstant : targetType.getEnumConstants()) {
                    if (value.intValue() == ((Enum<?>) enumConstant).ordinal()) {
                        return enumConstant;
                    }
                }
            }
        }
        return null;
    }

    private static Map<ConverterPairKey, DataConverter<?, ?>> loadConverters() {
        Map<ConverterPairKey, DataConverter<?, ?>> converterMap = new HashMap<>();
        DataConverterLoader.loadStringConverters(converterMap);
        DataConverterLoader.loadNumberConverters(converterMap);
        DataConverterLoader.loadDateConverters(converterMap);
        return converterMap;
    }

}
