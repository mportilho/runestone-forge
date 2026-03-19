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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultDataConversionService implements DataConversionService {

    private static final Map<ConverterPairKey, DataConverter<?, ?>> CONVERTERS = new ConcurrentHashMap<>(loadConverters());
    private static final ClassValue<EnumMetadata<?>> ENUM_METADATA_CACHE = new ClassValue<>() {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected EnumMetadata<?> computeValue(Class<?> type) {
            return new EnumMetadata(type);
        }
    };

    private final boolean throwIfNull;

    public DefaultDataConversionService() {
        this(true);
    }

    public DefaultDataConversionService(boolean throwIfNull) {
        this.throwIfNull = throwIfNull;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        if (targetType.isAssignableFrom(sourceType) || targetType.isPrimitive()) {
            return true;
        }
        if (targetType.isEnum() && (String.class.isAssignableFrom(sourceType) || Number.class.isAssignableFrom(sourceType))) {
            return true;
        }
        if (CONVERTERS.containsKey(new ConverterPairKey(sourceType, targetType))) {
            return true;
        }
        for (ConverterPairKey key : CONVERTERS.keySet()) {
            if (key.targetType().equals(targetType) && key.sourceType().isAssignableFrom(sourceType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings({ "unchecked", "null" })
    public <S, T> T convert(S source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "Target Type must be provided");
        if (source == null) {
            return null;
        } else if (targetType.isInstance(source)) {
            return (T) source;
        }
        T directNumberConversion = convertNumberTypes(source, targetType);
        if (directNumberConversion != null) {
            return directNumberConversion;
        }

        DataConverter<S, T> dataConverter = getConverter(source.getClass(), targetType);
        T convertedValue = dataConverter != null ? dataConverter.convert(source) : fallbackConversion(source, targetType);

        if (convertedValue == null && throwIfNull) {
            throw new NoDataConverterFoundException(source.getClass(), targetType);
        }
        return convertedValue;
    }

    @SuppressWarnings("unchecked")
    private <S, T> T convertNumberTypes(S source, Class<T> targetType) {
        if (!(source instanceof Number number)) {
            return null;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(number.intValue());
        }
        if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(number.longValue());
        }
        if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(number.doubleValue());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <S, T> DataConverter<S, T> getConverter(Class<?> sourceType, Class<?> targetType) {
        ConverterPairKey key = new ConverterPairKey(sourceType, targetType);
        DataConverter<?, ?> converter = CONVERTERS.get(key);
        if (converter != null) {
            return (DataConverter<S, T>) converter;
        }

        for (Map.Entry<ConverterPairKey, DataConverter<?, ?>> entry : CONVERTERS.entrySet()) {
            ConverterPairKey mapKey = entry.getKey();
            if (mapKey.targetType().equals(targetType) && mapKey.sourceType().isAssignableFrom(sourceType)) {
                converter = entry.getValue();
                CONVERTERS.put(key, converter);
                return (DataConverter<S, T>) converter;
            }
        }
        return null;
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

    @SuppressWarnings({ "unchecked", "null" })
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

    @SuppressWarnings({ "unchecked", "null" })
    private <S, T> T convertEnum(S source, Class<T> targetType) {
        if (targetType.isEnum()) {
            EnumMetadata<T> metadata = (EnumMetadata<T>) ENUM_METADATA_CACHE.get(targetType);
            if (source instanceof String value) {
                return metadata.getByName(value);
            } else if (source instanceof Number value) {
                return metadata.getByOrdinal(value.intValue());
            }
        }
        return null;
    }

    private static final class EnumMetadata<T> {
        private final T[] constants;
        private final Map<String, T> nameMap;

        public EnumMetadata(Class<T> enumType) {
            this.constants = enumType.getEnumConstants();
            this.nameMap = new HashMap<>();
            if (constants != null) {
                for (T constant : constants) {
                    nameMap.put(((Enum<?>) constant).name().toUpperCase(Locale.ROOT), constant);
                }
            }
        }

        public T getByName(String name) {
            return nameMap.get(name.toUpperCase(Locale.ROOT));
        }

        public T getByOrdinal(int ordinal) {
            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
            return null;
        }
    }

    private static Map<ConverterPairKey, DataConverter<?, ?>> loadConverters() {
        return DataConverterLoader.loadConverters();
    }

}
