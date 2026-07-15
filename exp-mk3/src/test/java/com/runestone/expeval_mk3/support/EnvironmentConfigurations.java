package com.runestone.expeval_mk3.support;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public final class EnvironmentConfigurations {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 7, 10);

    private EnvironmentConfigurations() {
    }

    public static ExpressionEnvironment complete() throws NoSuchMethodException {
        return completeBuilder().build();
    }

    public static ExpressionEnvironment.Builder completeBuilder() throws NoSuchMethodException {
        return ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("UTC"))
                .mathContext(new MathContext(18, RoundingMode.HALF_EVEN))
                .transcendentalMathContext(new MathContext(30, RoundingMode.HALF_UP))
                .maxCurrentItemDepth(3)
                .maxMaterializedSize(256)
                .maxFactorialInput(32)
                .boundaryCoercion(prefixedNumberConversionService())
                .externalSymbol("amount", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("businessDate", ScalarType.DATE, BUSINESS_DATE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("customer", customerProfileObjectType(), new CustomerProfile("Ana", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("labels", new MapType(ScalarType.STRING), Map.of("tier", "gold"),
                        ExternalSymbolOverwritePolicy.FIXED)
                .function(discountFunction())
                .registerJavaType(CustomerProfile.class);
    }

    public static Class<?> customerProfileClass() {
        return CustomerProfile.class;
    }

    public static ObjectType customerProfileObjectType() {
        return new ObjectType(CustomerProfile.class.getName());
    }

    public static DataConversionService prefixedNumberConversionService() {
        return prefixedNumberConversionService("test.prefixed-number", "test.prefixed-number-hash");
    }

    public static DataConversionService prefixedNumberConversionService(
            String conversionProfileIdentity,
            String conversionProfileHash) {
        return new PrefixedNumberConversionService(conversionProfileIdentity, conversionProfileHash);
    }

    public static List<RepresentativeEnvironmentConfiguration> representativeConfigurations() throws NoSuchMethodException {
        return List.of(
                new RepresentativeEnvironmentConfiguration("standard", ExpressionEnvironment.standard()),
                new RepresentativeEnvironmentConfiguration("tenant guarded", complete()),
                new RepresentativeEnvironmentConfiguration("custom coercion", ExpressionEnvironment.builder()
                        .boundaryCoercion(prefixedNumberConversionService())
                        .externalSymbol("amount", ScalarType.NUMBER, "points:10", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .build()),
                new RepresentativeEnvironmentConfiguration("Java type metadata", ExpressionEnvironment.builder()
                        .externalSymbol("customer", customerProfileObjectType(), new CustomerProfile("Ana", BigDecimal.TEN),
                                ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .registerJavaTypeWithPublicMethods(CustomerProfile.class)
                        .build()),
                new RepresentativeEnvironmentConfiguration("overloaded functions", ExpressionEnvironment.builder()
                        .function(discountFunction())
                        .function(discountStringOverloadFunction())
                        .build()));
    }

    public static FunctionDescriptor discountFunction() throws NoSuchMethodException {
        return descriptor(
                "acceptanceDiscount",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);
    }

    public static FunctionDescriptor discountStringOverloadFunction() throws NoSuchMethodException {
        return descriptor(
                "acceptanceDiscount",
                "textIdentity",
                List.of(ScalarType.STRING),
                ScalarType.STRING,
                FunctionPurity.FOLDABLE,
                String.class);
    }

    private static FunctionDescriptor descriptor(
            String languageName,
            String methodName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            FunctionPurity purity,
            Class<?>... parameterClasses) throws NoSuchMethodException {
        Method method = TestFunctions.class.getDeclaredMethod(methodName, parameterClasses);
        return FunctionDescriptor.fromMethod(languageName, method, parameterTypes, returnType, purity);
    }

    public record CustomerProfile(String name, BigDecimal score) {

        public BigDecimal scorePlus(BigDecimal increment) {
            return score.add(increment);
        }
    }

    private static final class PrefixedNumberConversionService implements DataConversionService {

        private final String conversionProfileIdentity;
        private final String conversionProfileHash;

        private PrefixedNumberConversionService(String conversionProfileIdentity, String conversionProfileHash) {
            this.conversionProfileIdentity = conversionProfileIdentity;
            this.conversionProfileHash = conversionProfileHash;
        }

        @Override
        public ConversionContext conversionContext() {
            return ConversionContext.standard();
        }

        @Override
        public String conversionProfileIdentity() {
            return conversionProfileIdentity;
        }

        @Override
        public String conversionProfileHash() {
            return conversionProfileHash;
        }

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return sourceType == String.class && targetType == BigDecimal.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S, T> T convert(S source, Class<T> targetType) {
            if (source instanceof String text && targetType == BigDecimal.class && text.startsWith("points:")) {
                return (T) new BigDecimal(text.substring("points:".length()));
            }
            throw new IllegalArgumentException("unsupported conversion");
        }

        @Override
        public <T> T copyFoldableValue(T value) {
            return value;
        }
    }

    public static final class TestFunctions {

        public static BigDecimal numberIdentity(BigDecimal value) {
            return value;
        }

        public static String textIdentity(String value) {
            return value;
        }
    }
}
