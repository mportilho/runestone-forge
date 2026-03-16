package com.runestone.expeval2.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.semantic.*;

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
        return switch (rawValue) {
            case Number number -> new NumberValue(convert(number, BigDecimal.class));
            case Boolean bool -> new BooleanValue(bool);
            case CharSequence text -> new StringValue(text.toString());
            case LocalDate localDate -> new DateValue(localDate);
            case LocalTime localTime -> new TimeValue(localTime);
            case LocalDateTime localDateTime -> new DateTimeValue(localDateTime);
            default -> {
                if (rawValue instanceof Iterable<?> || rawValue.getClass().isArray()) {
                    yield toVector(rawValue);
                }
                yield new StringValue(convert(rawValue, String.class));
            }
        };
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
