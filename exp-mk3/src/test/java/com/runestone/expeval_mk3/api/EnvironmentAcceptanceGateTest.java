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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class EnvironmentAcceptanceGateTest {

    @Test
    @DisplayName("complete equivalent environments receive different instance IDs")
    void completeEquivalentEnvironmentsReceiveDifferentInstanceIds() throws NoSuchMethodException {
        ExpressionEnvironment first = EnvironmentConfigurations.complete();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .registerJavaType(EnvironmentConfigurations.customerProfileClass())
                .function(EnvironmentConfigurations.discountFunction())
                .externalSymbol("labels", new MapType(ScalarType.STRING), Map.of("tier", "gold"),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("customer", EnvironmentConfigurations.customerProfileObjectType(),
                        new EnvironmentConfigurations.CustomerProfile("Ana", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 10),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("amount", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService())
                .maxMaterializedSize(256)
                .maxFactorialInput(32)
                .maxCurrentItemDepth(3)
                .transcendentalMathContext(new MathContext(30, RoundingMode.HALF_UP))
                .mathContext(new MathContext(18, RoundingMode.HALF_EVEN))
                .zoneId(ZoneId.of("UTC"))
                .build();

        assertThat(first).isNotSameAs(sameContentDifferentOrder);
        assertThat(first.environmentId()).isNotEqualTo(sameContentDifferentOrder.environmentId());
    }

    @Test
    @DisplayName("function overload resolution is deterministic for exact matches")
    void functionOverloadResolutionIsDeterministicForExactMatches() throws NoSuchMethodException {
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
        FunctionCatalog mismatchedOverloads = FunctionCatalog.builder()
                .register(number)
                .register(bool)
                .build();

        assertThat(firstOrder.resolve("coerce", List.of(ScalarType.STRING)).descriptor())
                .contains(text);
        assertThat(secondOrder.resolve("coerce", List.of(ScalarType.STRING)).descriptor())
                .contains(text);

        FunctionLookupResult notFound = mismatchedOverloads.resolve(
                "coerce",
                List.of(ScalarType.STRING));

        assertThat(notFound.status()).isEqualTo(FunctionLookupResult.Status.NOT_FOUND);
        assertThat(notFound.candidates()).isEmpty();
    }

    @Test
    @DisplayName("environment catalogs expose deterministic ordering independent of registration order")
    void environmentCatalogsExposeDeterministicOrderingIndependentOfRegistrationOrder() throws NoSuchMethodException {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .externalSymbol("zeta", ScalarType.STRING, "z", ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("alpha", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .registerJavaType(SecondaryProfile.class)
                .registerJavaType(EnvironmentConfigurations.customerProfileClass())
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .registerJavaType(EnvironmentConfigurations.customerProfileClass())
                .registerJavaType(SecondaryProfile.class)
                .externalSymbol("alpha", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("zeta", ScalarType.STRING, "z", ExternalSymbolOverwritePolicy.FIXED)
                .build();

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
    @DisplayName("standard environment contains every standard catalog")
    void standardEnvironmentContainsEveryStandardCatalog() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();
        FunctionCatalog functions = environment.functions();
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
                .containsOnly(
                        "asNumber",
                        "asText",
                        "asBool",
                        "asDate",
                        "asTime",
                        "asDateTime");
        assertThat(StandardBuiltInFunctions.expectedSignatures(BuiltInFunctionGroup.ASSERTION))
                .hasSize(36)
                .extracting(FunctionSignature::languageName)
                .contains(
                        "asNumber",
                        "asText",
                        "asBool",
                        "asDate",
                        "asTime",
                        "asDateTime");
        StandardBuiltInFunctions.validate(functions);
        assertThat(environment.collectionOperations().operationNames())
                .containsExactly("all", "any", "avg", "count", "keys", "map", "reduce", "sortBy", "sum", "values");
        assertThat(standardFunctionNames)
                .doesNotContain("all", "any", "avg", "count", "keys", "map", "reduce", "sortBy", "sum", "values");
    }

    @Test
    @DisplayName("environment and catalog invariants fail at construction time with stable errors")
    void environmentAndCatalogInvariantsFailAtConstructionTimeWithStableErrors() throws NoSuchMethodException {
        FunctionDescriptor discount = EnvironmentConfigurations.discountFunction();

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("amount", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("external symbol already declared: amount");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER, "not-a-number", ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .boundaryCoercion(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dataConversionService");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().maxCurrentItemDepth(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxCurrentItemDepth must not be negative");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().maxMaterializedSize(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxMaterializedSize must not be negative");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().maxFactorialInput(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxFactorialInput must not be negative");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol(CurrentTemporalValue.DATE.simpleName(), ScalarType.DATE, LocalDate.of(2026, 7, 10),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> FunctionCatalog.builder()
                .register(discount)
                .register(discount)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("function signature already registered: " + discount.signature().canonical())
                .hasMessageContaining("origins: custom function");
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
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .registerJavaType(UnmappablePropertyProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported Java member type: java.util.Optional");
    }

    @Test
    @DisplayName("environment construction publishes only valid homogeneous wildcard child metadata")
    void environmentConstructionPublishesOnlyValidHomogeneousWildcardChildMetadata() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(SecondaryProfile.class, "code")
                .build();

        assertThat(environment.javaTypes().find(SecondaryProfile.class))
                .get()
                .satisfies(descriptor -> {
                    assertThat(descriptor.wildcardChildren())
                            .extracting(JavaWildcardChildDescriptor::name)
                            .containsExactly("code");
                    assertThat(descriptor.wildcardChildType()).contains(ScalarType.STRING);
                });
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(HeterogeneousChildren.class, "text", "number")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wildcard child members must have one expression type");
    }

    @Test
    @DisplayName("Etapa 3 representative environment configurations are reusable and buildable")
    void etapa3RepresentativeEnvironmentConfigurationsAreReusableAndBuildable() throws NoSuchMethodException {
        List<RepresentativeEnvironmentConfiguration> configurations = EnvironmentConfigurations.representativeConfigurations();

        assertThat(configurations).extracting(RepresentativeEnvironmentConfiguration::name)
                .containsExactly(
                        "standard",
                        "tenant guarded",
                        "custom coercion",
                        "Java type metadata",
                        "overloaded functions");
        assertThat(configurations)
                .allSatisfy(configuration -> assertThat(configuration.environment().functions().size()).isPositive());

        Map<String, ExpressionEnvironment> byName = configurations.stream()
                .collect(Collectors.toUnmodifiableMap(
                        RepresentativeEnvironmentConfiguration::name,
                        RepresentativeEnvironmentConfiguration::environment));
        ExpressionEnvironment tenantGuarded = byName.get("tenant guarded");
        ExpressionEnvironment customCoercion = byName.get("custom coercion");
        ExpressionEnvironment javaTypeMetadata = byName.get("Java type metadata");
        ExpressionEnvironment overloadedFunctions = byName.get("overloaded functions");

        assertThat(tenantGuarded.maxMaterializedSize()).isEqualTo(256);
        assertThat(tenantGuarded.maxFactorialInput()).isEqualTo(32);
        assertThat(tenantGuarded.externalSymbols().asMap()).containsKeys("amount", "businessDate", "customer", "labels");
        assertThat(tenantGuarded.javaTypes().find(EnvironmentConfigurations.customerProfileClass())).isPresent();
        assertThat(tenantGuarded.functions().find(new FunctionSignature(
                "acceptanceDiscount",
                List.of(ScalarType.NUMBER)))).isPresent();

        assertThat(customCoercion.conversionProfileIdentity()).isEqualTo("test.prefixed-number");
        assertThat(customCoercion.externalSymbols().asMap().get("amount").defaultValue())
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(new BigDecimal("10"));

        assertThat(javaTypeMetadata.javaTypes().find(EnvironmentConfigurations.customerProfileClass()))
                .get()
                .satisfies(descriptor -> {
                    assertThat(descriptor.methodCount()).isPositive();
                    assertThat(JavaTypeCatalog.registeredMemberReturnNullability())
                            .isEqualTo(RuntimeNullability.NEVER_NULL);
                });

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
                "assertion", Set.of("asNumber", "asText", "asBool", "asDate", "asTime", "asDateTime"));
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

    static final class UnmappablePropertyProvider {

        public Optional<String> getValue() {
            return Optional.empty();
        }
    }

    record SecondaryProfile(String code) {
    }

    record HeterogeneousChildren(String text, BigDecimal number) {
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
