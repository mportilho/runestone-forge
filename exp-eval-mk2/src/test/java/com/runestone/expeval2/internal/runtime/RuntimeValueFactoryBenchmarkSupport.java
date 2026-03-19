package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
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

public final class RuntimeValueFactoryBenchmarkSupport {

    private final Factory baselineFactory;
    private final Factory optimizedFactory;
    private final BigDecimal exactNumber = new BigDecimal("42.75");
    private final String exactString = "alpha";
    private final BigDecimal[] numericArray;
    private final List<BigDecimal> numericList;

    public RuntimeValueFactoryBenchmarkSupport(int vectorSize) {
        baselineFactory = new BaselineFactory(new DefaultDataConversionService());
        optimizedFactory = new OptimizedFactory(new DefaultDataConversionService());

        numericArray = new BigDecimal[vectorSize];
        numericList = new ArrayList<>(vectorSize);
        for (int i = 0; i < vectorSize; i++) {
            BigDecimal value = BigDecimal.valueOf(i + 1L);
            numericArray[i] = value;
            numericList.add(value);
        }
    }

    public RuntimeValue baselineNumberScalarExact() {
        return baselineFactory.from(exactNumber, ScalarType.NUMBER);
    }

    public RuntimeValue optimizedNumberScalarExact() {
        return optimizedFactory.from(exactNumber, ScalarType.NUMBER);
    }

    public RuntimeValue baselineStringScalarExact() {
        return baselineFactory.from(exactString, ScalarType.STRING);
    }

    public RuntimeValue optimizedStringScalarExact() {
        return optimizedFactory.from(exactString, ScalarType.STRING);
    }

    public RuntimeValue baselineVectorArray() {
        return baselineFactory.from(numericArray, VectorType.INSTANCE);
    }

    public RuntimeValue optimizedVectorArray() {
        return optimizedFactory.from(numericArray, VectorType.INSTANCE);
    }

    public RuntimeValue baselineVectorIterable() {
        return baselineFactory.from(numericList, VectorType.INSTANCE);
    }

    public RuntimeValue optimizedVectorIterable() {
        return optimizedFactory.from(numericList, VectorType.INSTANCE);
    }

    private interface Factory {
        RuntimeValue from(Object rawValue, ResolvedType expectedType);
    }

    private static class BaselineFactory implements Factory {

        private final DataConversionService conversionService;

        private BaselineFactory(DataConversionService conversionService) {
            this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
        }

        @Override
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

        protected RuntimeValue.VectorValue toVector(Object rawValue) {
            List<RuntimeValue> elements = new ArrayList<>();
            if (rawValue instanceof Iterable<?> iterable) {
                for (Object element : iterable) {
                    elements.add(from(element, null));
                }
                return new RuntimeValue.VectorValue(elements);
            }
            if (rawValue.getClass().isArray()) {
                int length = Array.getLength(rawValue);
                for (int index = 0; index < length; index++) {
                    elements.add(from(Array.get(rawValue, index), null));
                }
                return new RuntimeValue.VectorValue(elements);
            }
            throw new IllegalArgumentException("rawValue is not a vector-compatible type");
        }

        protected <T> T convert(Object rawValue, Class<T> targetType) {
            T converted = conversionService.convert(rawValue, targetType);
            if (converted == null) {
                throw new IllegalArgumentException(
                        "cannot convert value '" + rawValue + "' to " + targetType.getSimpleName()
                );
            }
            return converted;
        }
    }

    private static final class OptimizedFactory extends BaselineFactory {

        private OptimizedFactory(DataConversionService conversionService) {
            super(conversionService);
        }

        @Override
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
                    case NUMBER -> rawValue instanceof BigDecimal value
                            ? new RuntimeValue.NumberValue(value)
                            : new RuntimeValue.NumberValue(convert(rawValue, BigDecimal.class));
                    case BOOLEAN -> rawValue instanceof Boolean value
                            ? new RuntimeValue.BooleanValue(value)
                            : new RuntimeValue.BooleanValue(convert(rawValue, Boolean.class));
                    case STRING -> rawValue instanceof String value
                            ? new RuntimeValue.StringValue(value)
                            : new RuntimeValue.StringValue(convert(rawValue, String.class));
                    case DATE -> rawValue instanceof LocalDate value
                            ? new RuntimeValue.DateValue(value)
                            : new RuntimeValue.DateValue(convert(rawValue, LocalDate.class));
                    case TIME -> rawValue instanceof LocalTime value
                            ? new RuntimeValue.TimeValue(value)
                            : new RuntimeValue.TimeValue(convert(rawValue, LocalTime.class));
                    case DATETIME -> rawValue instanceof LocalDateTime value
                            ? new RuntimeValue.DateTimeValue(value)
                            : new RuntimeValue.DateTimeValue(convert(rawValue, LocalDateTime.class));
                };
            }
            return switch (rawValue) {
                case BigDecimal number -> new RuntimeValue.NumberValue(number);
                case Number number -> new RuntimeValue.NumberValue(convert(number, BigDecimal.class));
                case Boolean bool -> new RuntimeValue.BooleanValue(bool);
                case String text -> new RuntimeValue.StringValue(text);
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

        @Override
        protected RuntimeValue.VectorValue toVector(Object rawValue) {
            if (rawValue instanceof List<?> list) {
                List<RuntimeValue> elements = new ArrayList<>(list.size());
                for (Object element : list) {
                    elements.add(from(element, null));
                }
                return new RuntimeValue.VectorValue(elements);
            }
            if (rawValue instanceof Iterable<?> iterable) {
                List<RuntimeValue> elements = new ArrayList<>();
                for (Object element : iterable) {
                    elements.add(from(element, null));
                }
                return new RuntimeValue.VectorValue(elements);
            }
            if (rawValue instanceof Object[] array) {
                List<RuntimeValue> elements = new ArrayList<>(array.length);
                for (Object element : array) {
                    elements.add(from(element, null));
                }
                return new RuntimeValue.VectorValue(elements);
            }
            if (rawValue.getClass().isArray()) {
                int length = Array.getLength(rawValue);
                List<RuntimeValue> elements = new ArrayList<>(length);
                for (int index = 0; index < length; index++) {
                    elements.add(from(Array.get(rawValue, index), null));
                }
                return new RuntimeValue.VectorValue(elements);
            }
            throw new IllegalArgumentException("rawValue is not a vector-compatible type");
        }
    }
}
