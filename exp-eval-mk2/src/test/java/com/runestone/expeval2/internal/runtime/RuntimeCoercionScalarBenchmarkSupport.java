package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public final class RuntimeCoercionScalarBenchmarkSupport {

    private final RuntimeCoercionService optimizedService;
    private final BaselineRuntimeCoercionService baselineService;
    private final RuntimeValue.NumberValue integralValue;
    private final RuntimeValue.NumberValue decimalValue;

    public RuntimeCoercionScalarBenchmarkSupport() {
        optimizedService = new RuntimeCoercionService(new DefaultDataConversionService());
        baselineService = new BaselineRuntimeCoercionService(new DefaultDataConversionService());
        integralValue = new RuntimeValue.NumberValue(BigDecimal.valueOf(42L));
        decimalValue = new RuntimeValue.NumberValue(new BigDecimal("42.5"));
    }

    public double baselineDoublePrimitive() {
        return (double) baselineService.coerce(decimalValue, double.class);
    }

    public double optimizedDoublePrimitive() {
        return (double) optimizedService.coerce(decimalValue, double.class);
    }

    public Double baselineDoubleWrapper() {
        return (Double) baselineService.coerce(decimalValue, Double.class);
    }

    public Double optimizedDoubleWrapper() {
        return (Double) optimizedService.coerce(decimalValue, Double.class);
    }

    public int baselineIntPrimitive() {
        return (int) baselineService.coerce(integralValue, int.class);
    }

    public int optimizedIntPrimitive() {
        return (int) optimizedService.coerce(integralValue, int.class);
    }

    public Integer baselineIntWrapper() {
        return (Integer) baselineService.coerce(integralValue, Integer.class);
    }

    public Integer optimizedIntWrapper() {
        return (Integer) optimizedService.coerce(integralValue, Integer.class);
    }

    public long baselineLongPrimitive() {
        return (long) baselineService.coerce(integralValue, long.class);
    }

    public long optimizedLongPrimitive() {
        return (long) optimizedService.coerce(integralValue, long.class);
    }

    public Long baselineLongWrapper() {
        return (Long) baselineService.coerce(integralValue, Long.class);
    }

    public Long optimizedLongWrapper() {
        return (Long) optimizedService.coerce(integralValue, Long.class);
    }

    private static final class BaselineRuntimeCoercionService {

        private final DataConversionService conversionService;

        private BaselineRuntimeCoercionService(DataConversionService conversionService) {
            this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
        }

        private BigDecimal asNumber(RuntimeValue value) {
            return switch (value) {
                case RuntimeValue.NumberValue numberValue -> numberValue.value();
                case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to number");
                default -> convert(value.raw(), BigDecimal.class);
            };
        }

        private boolean asBoolean(RuntimeValue value) {
            return switch (value) {
                case RuntimeValue.BooleanValue booleanValue -> booleanValue.value();
                case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to boolean");
                default -> convert(value.raw(), Boolean.class);
            };
        }

        private String asString(RuntimeValue value) {
            return switch (value) {
                case RuntimeValue.StringValue stringValue -> stringValue.value();
                case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to string");
                default -> convert(value.raw(), String.class);
            };
        }

        private LocalDate asDate(RuntimeValue value) {
            return switch (value) {
                case RuntimeValue.DateValue dateValue -> dateValue.value();
                case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to date");
                default -> convert(value.raw(), LocalDate.class);
            };
        }

        private LocalTime asTime(RuntimeValue value) {
            return switch (value) {
                case RuntimeValue.TimeValue timeValue -> timeValue.value();
                case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to time");
                default -> convert(value.raw(), LocalTime.class);
            };
        }

        private LocalDateTime asDateTime(RuntimeValue value) {
            return switch (value) {
                case RuntimeValue.DateTimeValue dateTimeValue -> dateTimeValue.value();
                case RuntimeValue.NullValue ignored -> throw new IllegalStateException("cannot coerce null to datetime");
                default -> convert(value.raw(), LocalDateTime.class);
            };
        }

        private Object coerce(RuntimeValue value, Class<?> targetType) {
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
            if (List.class.isAssignableFrom(targetType) && value instanceof RuntimeValue.VectorValue(List<RuntimeValue> elements)) {
                return elements.stream().map(RuntimeValue::raw).toList();
            }
            return convert(value.raw(), targetType);
        }

        private <T> T convert(Object rawValue, Class<T> targetType) {
            T converted = conversionService.convert(rawValue, targetType);
            if (converted == null) {
                throw new IllegalArgumentException(
                        "cannot convert value '" + rawValue + "' to " + targetType.getSimpleName()
                );
            }
            return converted;
        }
    }
}
