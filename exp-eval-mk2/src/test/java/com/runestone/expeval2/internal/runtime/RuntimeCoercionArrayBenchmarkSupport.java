package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RuntimeCoercionArrayBenchmarkSupport {

    private final RuntimeCoercionService optimizedService;
    private final BaselineRuntimeCoercionService baselineService;
    private final RuntimeValue.VectorValue numericVector;
    private final RuntimeValue.VectorValue comparableVector;

    public RuntimeCoercionArrayBenchmarkSupport(int vectorSize) {
        optimizedService = new RuntimeCoercionService(new DefaultDataConversionService());
        baselineService = new BaselineRuntimeCoercionService(new DefaultDataConversionService());
        numericVector = new RuntimeValue.VectorValue(createElements(vectorSize));
        comparableVector = new RuntimeValue.VectorValue(createElements(vectorSize));
    }

    public Object baselineBigDecimalArray() {
        return baselineService.coerce(numericVector, BigDecimal[].class);
    }

    public Object optimizedBigDecimalArray() {
        return optimizedService.coerce(numericVector, BigDecimal[].class);
    }

    public Object baselineDoubleArray() {
        return baselineService.coerce(numericVector, double[].class);
    }

    public Object optimizedDoubleArray() {
        return optimizedService.coerce(numericVector, double[].class);
    }

    public Object baselineComparableArray() {
        return baselineService.coerce(comparableVector, Comparable[].class);
    }

    public Object optimizedComparableArray() {
        return optimizedService.coerce(comparableVector, Comparable[].class);
    }

    private static List<RuntimeValue> createElements(int vectorSize) {
        List<RuntimeValue> elements = new ArrayList<>(vectorSize);
        for (int i = 0; i < vectorSize; i++) {
            elements.add(new RuntimeValue.NumberValue(BigDecimal.valueOf(i + 1L)));
        }
        return elements;
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
            if (value instanceof RuntimeValue.VectorValue vectorValue) {
                List<RuntimeValue> elements = vectorValue.elements();
                if (List.class.isAssignableFrom(targetType)) {
                    return elements.stream().map(RuntimeValue::raw).toList();
                }
                if (!targetType.isArray()) {
                    return convert(value.raw(), targetType);
                }
                Class<?> componentType = targetType.getComponentType();
                Object array = Array.newInstance(componentType, elements.size());
                for (int i = 0; i < elements.size(); i++) {
                    Array.set(array, i, coerce(elements.get(i), componentType));
                }
                return array;
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
