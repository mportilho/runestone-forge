package com.runestone.expeval_mk3.api;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import com.runestone.converters.PreparedDataConversion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEnvironmentProviderNumericContractTest {

    @Test
    @DisplayName("standard coercion prepares numeric provider parameters and results")
    void standardCoercionPreparesNumericProviderParametersAndResults() throws Throwable {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(StandardNumericProvider.class, FunctionPurity.PURE)
                .build();

        FunctionDescriptor descriptor = resolve(environment, "increment", ScalarType.NUMBER);

        assertThat(descriptor.returnType()).isEqualTo(ScalarType.NUMBER);
        assertThat(descriptor.implementationHandle().invoke(new BigDecimal("41")))
                .isEqualTo(new BigDecimal("42"));
    }

    @Test
    @DisplayName("configured coercion prepares custom Number contracts in both directions")
    void configuredCoercionPreparesCustomNumberContractsInBothDirections() throws Throwable {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(CustomNumericProvider.class, FunctionPurity.PURE)
                .boundaryCoercion(customNumberProfile(true, true))
                .build();

        FunctionDescriptor descriptor = resolve(environment, "increment", ScalarType.NUMBER);

        assertThat(descriptor.returnType()).isEqualTo(ScalarType.NUMBER);
        assertThat(descriptor.implementationHandle().invoke(new BigDecimal("9")))
                .isEqualTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("numeric provider contracts require each configured conversion direction")
    void numericProviderContractsRequireEachConfiguredConversionDirection() {
        ExpressionEnvironment.Builder missingParameterDirection = ExpressionEnvironment.builder()
                .functionsFrom(CustomNumericParameterProvider.class, FunctionPurity.PURE)
                .boundaryCoercion(customNumberProfile(false, true));
        ExpressionEnvironment.Builder missingResultDirection = ExpressionEnvironment.builder()
                .functionsFrom(CustomNumericResultProvider.class, FunctionPurity.PURE)
                .boundaryCoercion(customNumberProfile(true, false));

        assertThatThrownBy(missingParameterDirection::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BigDecimal.class.getName())
                .hasMessageContaining(CustomNumber.class.getName());
        assertThatThrownBy(missingResultDirection::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(CustomNumber.class.getName())
                .hasMessageContaining(BigDecimal.class.getName());
    }

    @Test
    @DisplayName("provider numeric resolution is independent of coercion registration order")
    void providerNumericResolutionIsIndependentOfCoercionRegistrationOrder() throws Throwable {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .functionsFrom(CustomNumericProvider.class, FunctionPurity.PURE)
                .boundaryCoercion(customNumberProfile(true, true))
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .boundaryCoercion(customNumberProfile(true, true))
                .functionsFrom(CustomNumericProvider.class, FunctionPurity.PURE)
                .build();

        assertThat(resolve(first, "increment", ScalarType.NUMBER).implementationHandle()
                .invoke(new BigDecimal("2"))).isEqualTo(new BigDecimal("3"));
        assertThat(resolve(second, "increment", ScalarType.NUMBER).implementationHandle()
                .invoke(new BigDecimal("2"))).isEqualTo(new BigDecimal("3"));
    }

    private static FunctionDescriptor resolve(
            ExpressionEnvironment environment,
            String name,
            ExpressionType... parameterTypes) {
        return environment.functions()
                .find(new FunctionSignature(name, List.of(parameterTypes)))
                .orElseThrow();
    }

    private static DataConversionService customNumberProfile(boolean inbound, boolean outbound) {
        return new CustomNumberConversionService(inbound, outbound);
    }

    public static final class StandardNumericProvider {
        public static long increment(int value) {
            return (long) value + 1;
        }
    }

    public static final class CustomNumericProvider {
        public static CustomNumber increment(CustomNumber value) {
            return new CustomNumber(value.decimalValue().add(BigDecimal.ONE));
        }
    }

    public static final class CustomNumericParameterProvider {
        public static BigDecimal value(CustomNumber value) {
            return value.decimalValue();
        }
    }

    public static final class CustomNumericResultProvider {
        public static CustomNumber value(BigDecimal value) {
            return new CustomNumber(value);
        }
    }

    public static final class CustomNumber extends Number {
        private final BigDecimal value;

        private CustomNumber(BigDecimal value) {
            this.value = value;
        }

        BigDecimal decimalValue() {
            return value;
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }
    }

    private static final class CustomNumberConversionService implements DataConversionService {
        private final boolean inbound;
        private final boolean outbound;

        private CustomNumberConversionService(boolean inbound, boolean outbound) {
            this.inbound = inbound;
            this.outbound = outbound;
        }

        @Override
        public ConversionContext conversionContext() {
            return ConversionContext.standard();
        }

        @Override
        public String conversionProfileIdentity() {
            return "test-custom-number:" + inbound + ':' + outbound;
        }

        @Override
        public String conversionProfileHash() {
            return conversionProfileIdentity();
        }

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return inbound && sourceType == BigDecimal.class && targetType == CustomNumber.class
                    || outbound && sourceType == CustomNumber.class && targetType == BigDecimal.class;
        }

        @Override
        public boolean canPrepareConversion(Class<?> sourceType, Class<?> targetType) {
            return canConvert(sourceType, targetType);
        }

        @Override
        public PreparedDataConversion prepareConversion(Class<?> sourceType, Class<?> targetType) {
            if (!canPrepareConversion(sourceType, targetType)) {
                throw new IllegalArgumentException("unsupported test conversion");
            }
            return source -> convert(source, targetType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S, T> T convert(S source, Class<T> targetType) {
            if (source instanceof BigDecimal decimal && targetType == CustomNumber.class && inbound) {
                return (T) new CustomNumber(decimal);
            }
            if (source instanceof CustomNumber custom && targetType == BigDecimal.class && outbound) {
                return (T) custom.decimalValue();
            }
            throw new IllegalArgumentException("unsupported test conversion");
        }

        @Override
        public <T> T copyFoldableValue(T value) {
            return value;
        }
    }

}
