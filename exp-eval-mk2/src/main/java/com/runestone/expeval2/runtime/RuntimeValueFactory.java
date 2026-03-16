package com.runestone.expeval2.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.semantic.ResolvedType;
import com.runestone.expeval2.semantic.ResolvedTypes;
import com.runestone.expeval2.semantic.ScalarType;
import com.runestone.expeval2.semantic.UnknownType;
import com.runestone.expeval2.semantic.VectorType;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RuntimeValueFactory {

    private final DataConversionService conversionService;

    public RuntimeValueFactory(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
    }

    public RuntimeValue from(Object rawValue) {
        return from(rawValue, null);
    }

    public RuntimeValue from(Object rawValue, ResolvedType expectedType) {
        if (rawValue == null) {
            return NullValue.INSTANCE;
        }
        if (rawValue instanceof RuntimeValue runtimeValue) {
            return runtimeValue;
        }
        ResolvedType effectiveType = expectedType == null || expectedType == UnknownType.INSTANCE
            ? ResolvedTypes.fromJavaType(rawValue.getClass())
            : expectedType;
        if (effectiveType == VectorType.INSTANCE) {
            return toVector(rawValue);
        }
        if (effectiveType instanceof ScalarType scalarType) {
            return switch (scalarType) {
                case NUMBER -> new NumberValue(convert(rawValue, BigDecimal.class));
                case BOOLEAN -> new BooleanValue(convert(rawValue, Boolean.class));
                case STRING -> new StringValue(convert(rawValue, String.class));
                case DATE -> new DateValue(convert(rawValue, LocalDate.class));
                case TIME -> new TimeValue(convert(rawValue, LocalTime.class));
                case DATETIME -> new DateTimeValue(convert(rawValue, LocalDateTime.class));
            };
        }
        if (rawValue instanceof Number number) {
            return new NumberValue(convert(number, BigDecimal.class));
        }
        if (rawValue instanceof Boolean bool) {
            return new BooleanValue(bool);
        }
        if (rawValue instanceof CharSequence text) {
            return new StringValue(text.toString());
        }
        if (rawValue instanceof LocalDate localDate) {
            return new DateValue(localDate);
        }
        if (rawValue instanceof LocalTime localTime) {
            return new TimeValue(localTime);
        }
        if (rawValue instanceof LocalDateTime localDateTime) {
            return new DateTimeValue(localDateTime);
        }
        if (rawValue instanceof Iterable<?> || rawValue.getClass().isArray()) {
            return toVector(rawValue);
        }
        return new StringValue(convert(rawValue, String.class));
    }

    public DataConversionService conversionService() {
        return conversionService;
    }

    private VectorValue toVector(Object rawValue) {
        List<RuntimeValue> elements = new ArrayList<>();
        if (rawValue instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                elements.add(from(element));
            }
            return new VectorValue(elements);
        }
        if (rawValue.getClass().isArray()) {
            int length = Array.getLength(rawValue);
            for (int index = 0; index < length; index++) {
                elements.add(from(Array.get(rawValue, index)));
            }
            return new VectorValue(elements);
        }
        throw new IllegalArgumentException("rawValue is not a vector-compatible type");
    }

    private <T> T convert(Object rawValue, Class<T> targetType) {
        T converted = conversionService.convert(rawValue, targetType);
        if (converted == null) {
            throw new IllegalArgumentException("cannot convert value '" + rawValue + "' to " + targetType.getSimpleName());
        }
        return converted;
    }
}
