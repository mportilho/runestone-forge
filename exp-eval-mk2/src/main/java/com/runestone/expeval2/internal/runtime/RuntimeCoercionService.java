package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.DataConversionService;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

final class RuntimeCoercionService {

    private final DataConversionService conversionService;

    public RuntimeCoercionService(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
    }

    public BigDecimal asNumber(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.NumberValue numberValue -> numberValue.value();
            case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to number");
            default -> convert(value.raw(), BigDecimal.class);
        };
    }

    public boolean asBoolean(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.BooleanValue booleanValue -> booleanValue.value();
            case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to boolean");
            default -> convert(value.raw(), Boolean.class);
        };
    }

    public String asString(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.StringValue stringValue -> stringValue.value();
            case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to string");
            default -> convert(value.raw(), String.class);
        };
    }

    public LocalDate asDate(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.DateValue dateValue -> dateValue.value();
            case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to date");
            default -> convert(value.raw(), LocalDate.class);
        };
    }

    public LocalTime asTime(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.TimeValue timeValue -> timeValue.value();
            case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to time");
            default -> convert(value.raw(), LocalTime.class);
        };
    }

    public LocalDateTime asDateTime(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.DateTimeValue dateTimeValue -> dateTimeValue.value();
            case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to datetime");
            default -> convert(value.raw(), LocalDateTime.class);
        };
    }

    public double asDouble(RuntimeValue value) {
        return asNumber(value).doubleValue();
    }

    public int asInt(RuntimeValue value) {
        return asNumber(value).intValue();
    }

    public long asLong(RuntimeValue value) {
        return asNumber(value).longValue();
    }

    public List<RuntimeValue> asVector(RuntimeValue value) {
        Objects.requireNonNull(value, "value must not be null");
        if (value instanceof RuntimeValue.VectorValue(List<RuntimeValue> elements)) {
            return elements;
        }
        throw new IllegalStateException("cannot coerce " + value.type() + " to vector");
    }

    public Object coerce(RuntimeValue value, Class<?> targetType) {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (targetType == RuntimeValue.class) {
            return value;
        }
        if (value == RuntimeValue.NullValue.INSTANCE) {
            return null;
        }
        if (targetType.isInstance(value.raw())) {
            return value.raw();
        }
        if (targetType == BigDecimal.class) {
            return asNumber(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return asDouble(value);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return asInt(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return asLong(value);
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
        if (value instanceof RuntimeValue.VectorValue(List<RuntimeValue> elements)) {
            if (List.class.isAssignableFrom(targetType)) {
                return elements.stream().map(RuntimeValue::raw).toList();
            }
            if (!targetType.isArray()) {
                return convert(value.raw(), targetType);
            }
            int n = elements.size();
            Class<?> componentType = targetType.getComponentType();

            if (componentType == BigDecimal.class) {
                BigDecimal[] array = new BigDecimal[n];
                for (int i = 0; i < n; i++) {
                    array[i] = asNumber(elements.get(i));
                }
                return array;
            }
            if (componentType == double.class) {
                double[] array = new double[n];
                for (int i = 0; i < n; i++) {
                    array[i] = asDouble(elements.get(i));
                }
                return array;
            }
            if (componentType == int.class) {
                int[] array = new int[n];
                for (int i = 0; i < n; i++) {
                    array[i] = asInt(elements.get(i));
                }
                return array;
            }
            if (componentType == long.class) {
                long[] array = new long[n];
                for (int i = 0; i < n; i++) {
                    array[i] = asLong(elements.get(i));
                }
                return array;
            }
            if (!componentType.isPrimitive()) {
                Object[] array = (Object[]) Array.newInstance(componentType, n);
                for (int i = 0; i < n; i++) {
                    array[i] = coerce(elements.get(i), componentType);
                }
                return array;
            }

            Object array = Array.newInstance(componentType, n);
            for (int i = 0; i < n; i++) {
                Array.set(array, i, coerce(elements.get(i), componentType));
            }
            return array;
        }

        return convert(value.raw(), targetType);
    }

    private <T> T convert(Object rawValue, Class<T> targetType) {
        T converted = conversionService.convert(rawValue, targetType);
        if (converted == null) {
            throw new IllegalArgumentException("cannot convert value '" + rawValue + "' to " + targetType.getSimpleName());
        }
        return converted;
    }
}
