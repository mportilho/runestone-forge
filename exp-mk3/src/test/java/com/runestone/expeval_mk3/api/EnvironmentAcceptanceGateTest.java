package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.support.EnvironmentConfigurations;
import com.runestone.expeval_mk3.support.RepresentativeEnvironmentConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class EnvironmentAcceptanceGateTest {

    @Test
    @DisplayName("complete equivalent environments produce the same environment ID")
    void completeEquivalentEnvironmentsProduceTheSameEnvironmentId() throws NoSuchMethodException {
        ExpressionEnvironment first = EnvironmentConfigurations.complete();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .registerJavaType(EnvironmentConfigurations.customerProfileClass())
                .function(EnvironmentConfigurations.discountFunction())
                .externalSymbolWithDefault("labels", new MapType(ScalarType.STRING), Map.of("tier", "gold"))
                .externalSymbol("customer", EnvironmentConfigurations.customerProfileObjectType())
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 10))
                .externalSymbol("amount", ScalarType.NUMBER)
                .deterministicBoundaryCoercion(
                        "acceptance-profile:v1",
                        EnvironmentConfigurations.prefixedNumberConversionService())
                .materializationLimit(256)
                .maxCurrentItemDepth(3)
                .strictMode(true)
                .transcendentalMathContext(new MathContext(30, RoundingMode.HALF_UP))
                .mathContext(new MathContext(18, RoundingMode.HALF_EVEN))
                .numericMode(NumericMode.FAST)
                .zoneId(ZoneId.of("UTC"))
                .build();

        assertThat(first).isNotSameAs(sameContentDifferentOrder);
        assertThat(first.environmentId()).isEqualTo(sameContentDifferentOrder.environmentId());
    }

    @Test
    @DisplayName("all environment identity inputs change the environment ID when relevant")
    void allEnvironmentIdentityInputsChangeTheEnvironmentIdWhenRelevant() throws NoSuchMethodException {
        ExpressionEnvironment baseline = EnvironmentConfigurations.complete();

        assertEnvironmentIdChanges("symbol name", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "total",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("symbol type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.STRING,
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("composite symbol type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        new VectorType(ScalarType.NUMBER),
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("collection symbol type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        new CollectionType(ScalarType.NUMBER),
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("map symbol type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        new MapType(ScalarType.NUMBER),
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("unknown symbol type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        UnknownType.INSTANCE,
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("object symbol type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        new ObjectType("AcceptanceObject"),
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("symbol default", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 11)).build());
        assertEnvironmentIdChanges("function return type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.discountFunctionReturningText(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("function language name", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.renamedDiscountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("function arity", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.twoArgumentDiscountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("function parameter type", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.discountStringOverloadFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("function purity", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.nonFoldableDiscountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("function implementation", baseline,
                EnvironmentConfigurations.completeBuilderWithJavaTypeProperties(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.alternateImplementationDiscountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("function overload set", baseline,
                EnvironmentConfigurations.completeBuilder()
                        .function(EnvironmentConfigurations.discountStringOverloadFunction())
                        .build());
        assertEnvironmentIdChanges("Java type catalog", baseline,
                EnvironmentConfigurations.completeBuilderWithPublicJavaMethods(
                        "amount",
                        ScalarType.NUMBER,
                        EnvironmentConfigurations.discountFunction(),
                        LocalDate.of(2026, 7, 10)).build());
        assertEnvironmentIdChanges("registered Java type class", baseline,
                EnvironmentConfigurations.completeBuilder()
                        .registerJavaType(SecondaryProfile.class)
                        .build());
        assertEnvironmentIdChanges("numeric mode", baseline,
                EnvironmentConfigurations.completeBuilder().numericMode(NumericMode.DECIMAL).build());
        assertEnvironmentIdChanges("time zone", baseline,
                EnvironmentConfigurations.completeBuilder().zoneId(ZoneId.of("America/Sao_Paulo")).build());
        assertEnvironmentIdChanges("materialization limit", baseline,
                EnvironmentConfigurations.completeBuilder().materializationLimit(257).build());
        assertEnvironmentIdChanges("current item limit", baseline,
                EnvironmentConfigurations.completeBuilder().maxCurrentItemDepth(4).build());
        assertEnvironmentIdChanges("math context", baseline,
                EnvironmentConfigurations.completeBuilder().mathContext(new MathContext(19, RoundingMode.HALF_EVEN)).build());
        assertEnvironmentIdChanges("transcendental math context", baseline,
                EnvironmentConfigurations.completeBuilder()
                        .transcendentalMathContext(new MathContext(31, RoundingMode.HALF_UP))
                        .build());
        assertEnvironmentIdChanges("strict mode", baseline, EnvironmentConfigurations.completeBuilder().strictMode(false).build());
        assertEnvironmentIdChanges("conversion profile", baseline,
                EnvironmentConfigurations.completeBuilder().deterministicBoundaryCoercion(
                        "acceptance-profile:v2",
                        EnvironmentConfigurations.prefixedNumberConversionService()).build());
    }

    @Test
    @DisplayName("function overload resolution is deterministic for matches and ambiguous cases")
    void functionOverloadResolutionIsDeterministicForMatchesAndAmbiguousCases() throws NoSuchMethodException {
        FunctionDescriptor text = descriptor(
                "coerce",
                "textIdentity",
                List.of(ScalarType.STRING),
                ScalarType.STRING,
                String.class);
        FunctionDescriptor number = descriptor(
                "coerce",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                BigDecimal.class);
        FunctionDescriptor bool = descriptor(
                "coerce",
                "booleanIdentity",
                List.of(ScalarType.BOOLEAN),
                ScalarType.BOOLEAN,
                Boolean.class);
        FunctionCatalog firstOrder = FunctionCatalog.builder()
                .register(text)
                .register(number)
                .register(bool)
                .build();
        FunctionCatalog secondOrder = FunctionCatalog.builder()
                .register(bool)
                .register(number)
                .register(text)
                .build();
        FunctionCatalog uniqueCoercionFallback = FunctionCatalog.builder()
                .register(number)
                .build();
        FunctionCatalog concreteAmbiguous = FunctionCatalog.builder()
                .register(number)
                .register(bool)
                .build();

        assertThat(firstOrder.resolve("coerce", List.of(ScalarType.STRING), BoundaryCoercion.standard()).descriptor())
                .contains(text);
        assertThat(secondOrder.resolve("coerce", List.of(ScalarType.STRING), BoundaryCoercion.standard()).descriptor())
                .contains(text);

        FunctionLookupResult firstAmbiguous = firstOrder.resolve(
                "coerce",
                List.of(UnknownType.INSTANCE),
                BoundaryCoercion.standard());
        FunctionLookupResult secondAmbiguous = secondOrder.resolve(
                "coerce",
                List.of(UnknownType.INSTANCE),
                BoundaryCoercion.standard());

        assertThat(firstAmbiguous.status()).isEqualTo(FunctionLookupResult.Status.AMBIGUOUS);
        assertThat(secondAmbiguous.status()).isEqualTo(FunctionLookupResult.Status.AMBIGUOUS);
        assertThat(firstAmbiguous.candidates()).extracting(FunctionDescriptor::signature)
                .containsExactly(bool.signature(), number.signature(), text.signature());
        assertThat(secondAmbiguous.candidates()).extracting(FunctionDescriptor::signature)
                .containsExactly(bool.signature(), number.signature(), text.signature());

        FunctionLookupResult coercionFallback = uniqueCoercionFallback.resolve(
                "coerce",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());
        FunctionLookupResult concreteCoercionAmbiguous = concreteAmbiguous.resolve(
                "coerce",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());

        assertThat(coercionFallback.status()).isEqualTo(FunctionLookupResult.Status.BOUNDARY_COERCION_MATCH);
        assertThat(coercionFallback.descriptor()).contains(number);
        assertThat(concreteCoercionAmbiguous.status()).isEqualTo(FunctionLookupResult.Status.AMBIGUOUS);
        assertThat(concreteCoercionAmbiguous.candidates()).extracting(FunctionDescriptor::signature)
                .containsExactly(bool.signature(), number.signature());
    }

    @Test
    @DisplayName("environment catalogs expose deterministic ordering independent of registration order")
    void environmentCatalogsExposeDeterministicOrderingIndependentOfRegistrationOrder() throws NoSuchMethodException {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .externalSymbol("zeta", ScalarType.STRING)
                .externalSymbol("alpha", ScalarType.NUMBER)
                .registerJavaType(SecondaryProfile.class)
                .registerJavaType(EnvironmentConfigurations.customerProfileClass())
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .registerJavaType(EnvironmentConfigurations.customerProfileClass())
                .registerJavaType(SecondaryProfile.class)
                .externalSymbol("alpha", ScalarType.NUMBER)
                .externalSymbol("zeta", ScalarType.STRING)
                .build();

        assertThat(first.environmentId()).isEqualTo(second.environmentId());
        assertThat(first.externalSymbols().values()).extracting(ExternalSymbol::name)
                .containsExactly("alpha", "zeta");
        assertThat(second.externalSymbols().values()).extracting(ExternalSymbol::name)
                .containsExactly("alpha", "zeta");
        assertThat(first.javaTypes().values()).extracting(descriptor -> descriptor.javaType().getName())
                .containsExactly(
                        SecondaryProfile.class.getName(),
                        EnvironmentConfigurations.customerProfileClass().getName());
        assertThat(second.javaTypes().values()).extracting(descriptor -> descriptor.javaType().getName())
                .containsExactly(
                        SecondaryProfile.class.getName(),
                        EnvironmentConfigurations.customerProfileClass().getName());

        FunctionCatalog firstFunctions = FunctionCatalog.builder()
                .register(EnvironmentConfigurations.discountStringOverloadFunction())
                .register(EnvironmentConfigurations.discountFunction())
                .build();
        FunctionCatalog secondFunctions = FunctionCatalog.builder()
                .register(EnvironmentConfigurations.discountFunction())
                .register(EnvironmentConfigurations.discountStringOverloadFunction())
                .build();

        assertThat(firstFunctions.values()).extracting(FunctionDescriptor::signature)
                .containsExactlyElementsOf(secondFunctions.values().stream()
                        .map(FunctionDescriptor::signature)
                        .toList());
    }

    @Test
    @DisplayName("standard environment contains every standard built-in group and assertion function")
    void standardEnvironmentContainsEveryStandardBuiltInGroupAndAssertionFunction() {
        FunctionCatalog functions = ExpressionEnvironment.standard().functions();
        Set<String> standardFunctionNames = functions.values().stream()
                .map(FunctionDescriptor::languageName)
                .collect(Collectors.toUnmodifiableSet());

        for (Map.Entry<String, Set<String>> group : expectedBuiltInFunctionNamesByGroup().entrySet()) {
            assertThat(standardFunctionNames)
                    .as("built-in group %s", group.getKey())
                    .containsAll(group.getValue());
        }
        for (BuiltInFunctionGroup group : BuiltInFunctionGroup.values()) {
            assertThat(functions.values())
                    .extracting(FunctionDescriptor::signature)
                    .as("built-in group %s", group)
                    .containsAll(StandardBuiltInFunctions.expectedSignatures(group));
        }
        assertThat(StandardBuiltInFunctions.expectedSignatures(BuiltInFunctionGroup.ASSERTION))
                .extracting(FunctionSignature::languageName)
                .containsExactlyInAnyOrder(
                        "asNumber",
                        "asText",
                        "asBool",
                        "asDate",
                        "asTime",
                        "asDateTime",
                        "asVector");
        StandardBuiltInFunctions.validate(functions);
    }

    @Test
    @DisplayName("environment and catalog invariants fail at construction time with stable diagnostics")
    void environmentAndCatalogInvariantsFailAtConstructionTimeWithStableDiagnostics() throws NoSuchMethodException {
        FunctionDescriptor discount = EnvironmentConfigurations.discountFunction();

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("amount")
                .externalSymbol("amount")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("external symbol already declared: amount");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "not-a-number")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().conversionProfileIdentity(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("conversionProfileIdentity must not be blank");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .boundaryCoercion("custom-profile:v1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dataConversionService");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().maxCurrentItemDepth(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxCurrentItemDepth must not be negative");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().materializationLimit(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("materializationLimit must not be negative");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol(CurrentTemporalValue.DATE.simpleName())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> FunctionCatalog.builder()
                .register(discount)
                .register(discount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("function signature already registered: " + discount.signature().canonical());
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaType(DuplicatePropertyProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duplicate Java property name: active");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWithPublicMethods(DuplicateMethodProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate Java member method signature");
    }

    @Test
    @DisplayName("Etapa 3 representative environment configurations are reusable and buildable")
    void etapa3RepresentativeEnvironmentConfigurationsAreReusableAndBuildable() throws NoSuchMethodException {
        List<RepresentativeEnvironmentConfiguration> configurations = EnvironmentConfigurations.representativeConfigurations();

        assertThat(configurations).extracting(RepresentativeEnvironmentConfiguration::name)
                .containsExactly(
                        "standard",
                        "strict fast tenant",
                        "custom coercion",
                        "Java type metadata",
                        "overloaded functions");
        assertThat(configurations)
                .allSatisfy(configuration -> assertThat(configuration.environment().functions().size()).isPositive());

        Map<String, ExpressionEnvironment> byName = configurations.stream()
                .collect(Collectors.toUnmodifiableMap(
                        RepresentativeEnvironmentConfiguration::name,
                        RepresentativeEnvironmentConfiguration::environment));
        ExpressionEnvironment strictFastTenant = byName.get("strict fast tenant");
        ExpressionEnvironment customCoercion = byName.get("custom coercion");
        ExpressionEnvironment javaTypeMetadata = byName.get("Java type metadata");
        ExpressionEnvironment overloadedFunctions = byName.get("overloaded functions");

        assertThat(strictFastTenant.numericMode()).isEqualTo(NumericMode.FAST);
        assertThat(strictFastTenant.strictMode()).isTrue();
        assertThat(strictFastTenant.externalSymbols().asMap()).containsKeys("amount", "businessDate", "customer", "labels");
        assertThat(strictFastTenant.javaTypes().find(EnvironmentConfigurations.customerProfileClass())).isPresent();
        assertThat(strictFastTenant.functions().find(new FunctionSignature(
                "acceptanceDiscount",
                List.of(ScalarType.NUMBER)))).isPresent();

        assertThat(customCoercion.conversionProfileIdentity()).isEqualTo("custom-profile:v1");
        assertThat(customCoercion.externalSymbols().asMap().get("amount").defaultValue())
                .get()
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(new BigDecimal("10"));

        assertThat(javaTypeMetadata.javaTypes().find(EnvironmentConfigurations.customerProfileClass()))
                .get()
                .satisfies(descriptor -> assertThat(descriptor.methodCount()).isPositive());

        assertThat(overloadedFunctions.functions().find(new FunctionSignature(
                "acceptanceDiscount",
                List.of(ScalarType.NUMBER)))).isPresent();
        assertThat(overloadedFunctions.functions().find(new FunctionSignature(
                "acceptanceDiscount",
                List.of(ScalarType.STRING)))).isPresent();
    }

    private static Map<String, Set<String>> expectedBuiltInFunctionNamesByGroup() {
        return Map.of(
                "math", Set.of(
                        "abs",
                        "sqrt",
                        "mean",
                        "geometricMean",
                        "harmonicMean",
                        "variance",
                        "stdDev",
                        "meanDev",
                        "rule3d",
                        "rule3i",
                        "distribute",
                        "spread"),
                "transcendental", Set.of(
                        "sin",
                        "cos",
                        "tan",
                        "asin",
                        "acos",
                        "atan",
                        "atan2",
                        "sinh",
                        "cosh",
                        "tanh",
                        "asinh",
                        "acosh",
                        "atanh",
                        "ln",
                        "lb",
                        "log",
                        "lnFast",
                        "lbFast",
                        "logFast"),
                "string", Set.of(
                        "concat",
                        "toUpper",
                        "toLower",
                        "trim",
                        "trimLeft",
                        "trimRight",
                        "substring",
                        "substringBefore",
                        "substringAfter",
                        "substringBeforeLast",
                        "substringAfterLast",
                        "padLeft",
                        "padRight",
                        "repeat",
                        "replace",
                        "replaceFirst",
                        "replaceAll",
                        "indexOf",
                        "lastIndexOf",
                        "startsWith",
                        "endsWith",
                        "contains",
                        "isEmpty",
                        "isBlank",
                        "length",
                        "split",
                        "join"),
                "dateTime", Set.of(
                        "secondsBetween",
                        "minutesBetween",
                        "hoursBetween",
                        "daysBetween",
                        "monthsBetween",
                        "yearsBetween",
                        "setDay",
                        "setMonth",
                        "setYear",
                        "setHours",
                        "setMinutes",
                        "setSeconds",
                        "setMidnight",
                        "setMidday",
                        "addDay",
                        "addMonth",
                        "addYear",
                        "addHours",
                        "addMinutes",
                        "addSeconds",
                        "subDay",
                        "subMonth",
                        "subYear",
                        "subHours",
                        "subMinutes",
                        "subSeconds"),
                "comparable", Set.of("max", "min"),
                "financial", Set.of("fv", "pv", "npv", "pmt", "nper", "ipmt", "ppmt"),
                "assertion", Set.of("asNumber", "asText", "asBool", "asDate", "asTime", "asDateTime", "asVector"));
    }

    private static FunctionDescriptor descriptor(
            String languageName,
            String methodName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            Class<?>... parameterClasses) throws NoSuchMethodException {
        Method method = TestFunctions.class.getDeclaredMethod(methodName, parameterClasses);
        return FunctionDescriptor.fromMethod(languageName, method, parameterTypes, returnType, FunctionPurity.FOLDABLE);
    }

    private static void assertEnvironmentIdChanges(
            String variation,
            ExpressionEnvironment baseline,
            ExpressionEnvironment changed) {
        assertThat(changed.environmentId())
                .as(variation)
                .isNotEqualTo(baseline.environmentId());
    }

    static final class DuplicatePropertyProvider {

        public boolean getActive() {
            return true;
        }

        public boolean isActive() {
            return true;
        }
    }

    static final class DuplicateMethodProvider {

        public BigDecimal same(BigDecimal value) {
            return value;
        }

        public int same(int value) {
            return value;
        }
    }

    record SecondaryProfile(String code) {
    }

    static final class TestFunctions {

        static BigDecimal numberIdentity(BigDecimal value) {
            return value;
        }

        static String textIdentity(String value) {
            return value;
        }

        static Boolean booleanIdentity(Boolean value) {
            return value;
        }
    }

}
