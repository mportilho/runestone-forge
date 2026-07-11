package com.runestone.expeval_mk3.support;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.NumericMode;
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
import java.util.Objects;

public final class EnvironmentConfigurations {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 7, 10);

    private EnvironmentConfigurations() {
    }

    public static ExpressionEnvironment complete() throws NoSuchMethodException {
        return completeBuilder().build();
    }

    public static ExpressionEnvironment.Builder completeBuilder() throws NoSuchMethodException {
        return completeBuilderWithJavaTypeProperties(
                "amount",
                ScalarType.NUMBER,
                discountFunction(),
                BUSINESS_DATE);
    }

    public static ExpressionEnvironment.Builder completeBuilderWithJavaTypeProperties(
            String amountSymbolName,
            ExpressionType amountType,
            FunctionDescriptor function,
            LocalDate businessDate) {
        return completeBuilder(amountSymbolName, amountType, function, JavaTypeExposure.PROPERTIES, businessDate);
    }

    public static ExpressionEnvironment.Builder completeBuilderWithPublicJavaMethods(
            String amountSymbolName,
            ExpressionType amountType,
            FunctionDescriptor function,
            LocalDate businessDate) {
        return completeBuilder(amountSymbolName, amountType, function, JavaTypeExposure.PUBLIC_METHODS, businessDate);
    }

    public static Class<?> customerProfileClass() {
        return CustomerProfile.class;
    }

    public static ObjectType customerProfileObjectType() {
        return new ObjectType(CustomerProfile.class.getName());
    }

    public static DataConversionService prefixedNumberConversionService() {
        return new PrefixedNumberConversionService();
    }

    private static ExpressionEnvironment.Builder completeBuilder(
            String amountSymbolName,
            ExpressionType amountType,
            FunctionDescriptor function,
            JavaTypeExposure javaTypeExposure,
            LocalDate businessDate) {
        Objects.requireNonNull(amountSymbolName, "amountSymbolName");
        Objects.requireNonNull(amountType, "amountType");
        Objects.requireNonNull(function, "function");
        Objects.requireNonNull(javaTypeExposure, "javaTypeExposure");
        Objects.requireNonNull(businessDate, "businessDate");
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("UTC"))
                .numericMode(NumericMode.FAST)
                .mathContext(new MathContext(18, RoundingMode.HALF_EVEN))
                .transcendentalMathContext(new MathContext(30, RoundingMode.HALF_UP))
                .strictMode(true)
                .maxCurrentItemDepth(3)
                .materializationLimit(256)
                .deterministicBoundaryCoercion("acceptance-profile:v1", prefixedNumberConversionService())
                .externalSymbol(amountSymbolName, amountType)
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, businessDate)
                .externalSymbol("customer", customerProfileObjectType())
                .externalSymbolWithDefault("labels", new MapType(ScalarType.STRING), Map.of("tier", "gold"))
                .function(function);
        return switch (javaTypeExposure) {
            case PROPERTIES -> builder.registerJavaType(CustomerProfile.class);
            case PUBLIC_METHODS -> builder.registerJavaTypeWithPublicMethods(CustomerProfile.class);
        };
    }

    public static List<RepresentativeEnvironmentConfiguration> representativeConfigurations() throws NoSuchMethodException {
        return List.of(
                new RepresentativeEnvironmentConfiguration("standard", ExpressionEnvironment.standard()),
                new RepresentativeEnvironmentConfiguration("strict fast tenant", complete()),
                new RepresentativeEnvironmentConfiguration("custom coercion", ExpressionEnvironment.builder()
                        .boundaryCoercion("custom-profile:v1", prefixedNumberConversionService())
                        .externalSymbolWithDefault("amount", ScalarType.NUMBER, "points:10")
                        .build()),
                new RepresentativeEnvironmentConfiguration("Java type metadata", ExpressionEnvironment.builder()
                        .externalSymbol("customer", customerProfileObjectType())
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

    public static FunctionDescriptor alternateImplementationDiscountFunction() throws NoSuchMethodException {
        return descriptor(
                "acceptanceDiscount",
                "numberIdentityCopy",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);
    }

    public static FunctionDescriptor renamedDiscountFunction() throws NoSuchMethodException {
        return descriptor(
                "renamedAcceptanceDiscount",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);
    }

    public static FunctionDescriptor twoArgumentDiscountFunction() throws NoSuchMethodException {
        return descriptor(
                "acceptanceDiscount",
                "numberPairIdentity",
                List.of(ScalarType.NUMBER, ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class,
                BigDecimal.class);
    }

    public static FunctionDescriptor nonFoldableDiscountFunction() throws NoSuchMethodException {
        return descriptor(
                "acceptanceDiscount",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.PURE,
                BigDecimal.class);
    }

    public static FunctionDescriptor discountFunctionReturningText() throws NoSuchMethodException {
        return descriptor(
                "acceptanceDiscount",
                "numberAsText",
                List.of(ScalarType.NUMBER),
                ScalarType.STRING,
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

    private enum JavaTypeExposure {
        PROPERTIES,
        PUBLIC_METHODS
    }

    public record CustomerProfile(String name, BigDecimal score) {

        public BigDecimal scorePlus(BigDecimal increment) {
            return score.add(increment);
        }
    }

    private static final class PrefixedNumberConversionService implements DataConversionService {

        @Override
        public ConversionContext conversionContext() {
            return ConversionContext.standard();
        }

        @Override
        public String conversionProfileIdentity() {
            return "test.prefixed-number";
        }

        @Override
        public String conversionProfileHash() {
            return "test.prefixed-number";
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

        public static BigDecimal numberIdentityCopy(BigDecimal value) {
            return value;
        }

        public static BigDecimal numberPairIdentity(BigDecimal first, BigDecimal ignoredSecond) {
            return first;
        }

        public static String numberAsText(BigDecimal value) {
            return value.toPlainString();
        }

        public static String textIdentity(String value) {
            return value;
        }
    }
}
