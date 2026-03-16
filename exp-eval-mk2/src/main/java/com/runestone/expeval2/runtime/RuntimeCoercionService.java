package com.runestone.expeval2.runtime;

import com.runestone.converters.DataConversionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public final class RuntimeCoercionService {

    private final DataConversionService conversionService;

    public RuntimeCoercionService(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
    }

    public BigDecimal asNumber(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case NumberValue numberValue -> numberValue.value();
            case NullValue ignored -> throw new IllegalStateException("cannot coerce null to number");
            default -> convert(value.raw(), BigDecimal.class, "number");
        };
    }

    public boolean asBoolean(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case BooleanValue booleanValue -> booleanValue.value();
            case NullValue ignored -> throw new IllegalStateException("cannot coerce null to boolean");
            default -> convert(value.raw(), Boolean.class, "boolean");
        };
    }

    public String asString(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case StringValue stringValue -> stringValue.value();
            case NullValue ignored -> throw new IllegalStateException("cannot coerce null to string");
            default -> convert(value.raw(), String.class, "string");
        };
    }

    public LocalDate asDate(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case DateValue dateValue -> dateValue.value();
            case NullValue ignored -> throw new IllegalStateException("cannot coerce null to date");
            default -> convert(value.raw(), LocalDate.class, "date");
        };
    }

    public LocalTime asTime(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case TimeValue timeValue -> timeValue.value();
            case NullValue ignored -> throw new IllegalStateException("cannot coerce null to time");
            default -> convert(value.raw(), LocalTime.class, "time");
        };
    }

    public LocalDateTime asDateTime(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case DateTimeValue dateTimeValue -> dateTimeValue.value();
            case NullValue ignored -> throw new IllegalStateException("cannot coerce null to datetime");
            default -> convert(value.raw(), LocalDateTime.class, "datetime");
        };
    }

    public List<RuntimeValue> asVector(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        if (value instanceof VectorValue vectorValue) {
            return vectorValue.elements();
        }
        throw new IllegalStateException("cannot coerce non-vector value to vector");
    }

    public Object coerce(RuntimeValue value, Class<?> targetType) {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (targetType == RuntimeValue.class) {
            return value;
        }
        if (value == NullValue.INSTANCE) {
            if (targetType.isPrimitive()) {
                throw new IllegalStateException("cannot coerce null to primitive type " + targetType.getSimpleName());
            }
            return null;
        }
        if (targetType == BigDecimal.class) {
            return asNumber(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return asBoolean(value);
        }
        if (targetType == String.class) {
            return asString(value);
        }
        if (targetType == LocalDate.class) {
            return asDate(value);
        }
        if (targetType == LocalTime.class) {
            return asTime(value);
        }
        if (targetType == LocalDateTime.class) {
            return asDateTime(value);
        }
        if (List.class.isAssignableFrom(targetType) && value instanceof VectorValue vectorValue) {
            return vectorValue.elements().stream().map(RuntimeValue::raw).toList();
        }
        if (targetType.isInstance(value.raw())) {
            return value.raw();
        }
        Object converted = conversionService.convert(value.raw(), targetType);
        if (converted == null) {
            throw new IllegalStateException("cannot coerce value to " + targetType.getSimpleName());
        }
        return converted;
    }

    private <T> T convert(Object rawValue, Class<T> targetType, String kind) {
        T converted = conversionService.convert(rawValue, targetType);
        if (converted == null) {
            throw new IllegalStateException("cannot coerce value to " + kind);
        }
        return converted;
    }
}
