package com.runestone.expeval2.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ResolvedTypes;
import com.runestone.expeval2.types.ScalarType;
import com.runestone.expeval2.types.UnknownType;
import com.runestone.expeval2.types.VectorType;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class RuntimeValueFactory {

    private final DataConversionService conversionService;

    public RuntimeValueFactory(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
    }

    public RuntimeValue from(Object rawValue) {
        return from(rawValue, null);
    }

    public RuntimeValue from(Object rawValue, ResolvedType expectedType) {
        if (rawValue == null) {
            return RuntimeValue.NullValue.INSTANCE;
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
                case NUMBER -> new RuntimeValue.NumberValue(convert(rawValue, BigDecimal.class));
                case BOOLEAN -> new RuntimeValue.BooleanValue(convert(rawValue, Boolean.class));
                case STRING -> new RuntimeValue.StringValue(convert(rawValue, String.class));
                case DATE -> new RuntimeValue.DateValue(convert(rawValue, LocalDate.class));
                case TIME -> new RuntimeValue.TimeValue(convert(rawValue, LocalTime.class));
                case DATETIME -> new RuntimeValue.DateTimeValue(convert(rawValue, LocalDateTime.class));
            };
        }
        return switch (rawValue) {
            case Number number -> new RuntimeValue.NumberValue(convert(number, BigDecimal.class));
            case Boolean bool -> new RuntimeValue.BooleanValue(bool);
            case CharSequence text -> new RuntimeValue.StringValue(text.toString());
            case LocalDate localDate -> new RuntimeValue.DateValue(localDate);
            case LocalTime localTime -> new RuntimeValue.TimeValue(localTime);
            case LocalDateTime localDateTime -> new RuntimeValue.DateTimeValue(localDateTime);
            default -> {
                if (rawValue instanceof Iterable<?> || rawValue.getClass().isArray()) {
                    yield toVector(rawValue);
                }
                yield new RuntimeValue.StringValue(convert(rawValue, String.class));
            }
        };
    }

    private RuntimeValue.VectorValue toVector(Object rawValue) {
        List<RuntimeValue> elements = new ArrayList<>();
        if (rawValue instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                elements.add(from(element));
            }
            return new RuntimeValue.VectorValue(elements);
        }
        if (rawValue.getClass().isArray()) {
            int length = Array.getLength(rawValue);
            for (int index = 0; index < length; index++) {
                elements.add(from(Array.get(rawValue, index)));
            }
            return new RuntimeValue.VectorValue(elements);
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
