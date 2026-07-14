package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.support.EnvironmentConfigurations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandardBuiltInFunctionsTest {

    @Test
    @DisplayName("standard environment includes official built-in function groups by default")
    void standardEnvironmentIncludesOfficialBuiltInFunctionGroupsByDefault() {
        FunctionCatalog functions = ExpressionEnvironment.standard().functions();

        assertThat(functions.size()).isGreaterThan(0);
        assertThat(functions.values())
                .extracting(FunctionDescriptor::languageName)
                .contains(
                        "abs",
                        "sqrt",
                        "mean",
                        "sin",
                        "ln",
                        "lnFast",
                        "toUpper",
                        "daysBetween",
                        "max",
                        "pmt",
                        "asNumber",
                        "asText",
                        "asBool",
                        "asDate",
                        "asTime",
                        "asDateTime");
    }

    @Test
    @DisplayName("built-in descriptors are pure foldable functions invokable without expression runtime")
    void builtInDescriptorsArePureFoldableFunctionsInvokableWithoutExpressionRuntime() throws Throwable {
        FunctionCatalog functions = ExpressionEnvironment.standard().functions();

        assertThat((BigDecimal) invoke(functions, "abs", List.of(ScalarType.NUMBER), new BigDecimal("-12.50")))
                .isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat((BigDecimal) invoke(functions, "sqrt", List.of(ScalarType.NUMBER), new BigDecimal("4")))
                .isEqualByComparingTo(new BigDecimal("2"));
        assertThat((BigDecimal) invoke(functions, "mean", List.of(new VectorType(ScalarType.NUMBER)),
                List.of(BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3))))
                .isEqualByComparingTo(new BigDecimal("2"));

        assertThat((BigDecimal) invoke(functions, "sin", List.of(ScalarType.NUMBER), BigDecimal.ZERO))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) invoke(functions, "ln", List.of(ScalarType.NUMBER), BigDecimal.ONE))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) invoke(functions, "lnFast", List.of(ScalarType.NUMBER), BigDecimal.ONE))
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(invoke(functions, "toUpper", List.of(ScalarType.STRING), "runestone"))
                .isEqualTo("RUNESTONE");
        assertThat((BigDecimal) invoke(functions, "daysBetween", List.of(ScalarType.DATE, ScalarType.DATE),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 9)))
                .isEqualByComparingTo(new BigDecimal("8"));
        assertThat(invoke(functions, "max", List.of(new VectorType(ScalarType.STRING)), List.of("b", "a", "c")))
                .isEqualTo("c");
        assertThat((BigDecimal) invoke(functions, "pmt", List.of(
                ScalarType.NUMBER,
                ScalarType.NUMBER,
                ScalarType.NUMBER,
                ScalarType.NUMBER,
                ScalarType.BOOLEAN),
                BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(-100), BigDecimal.ZERO, false))
                .isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("math built-in group requires abs and sqrt as declared members")
    void mathBuiltInGroupRequiresAbsAndSqrtAsDeclaredMembers() throws NoSuchMethodException {
        List<FunctionDescriptor> completeMathGroup = standardGroupDescriptors(BuiltInFunctionGroup.MATH);
        List<FunctionDescriptor> missingSqrt = descriptorsExcept(completeMathGroup, "sqrt");
        List<FunctionDescriptor> missingAbs = descriptorsExcept(completeMathGroup, "abs");

        assertThatThrownBy(() -> StandardBuiltInFunctions.validateGroup(BuiltInFunctionGroup.MATH, missingSqrt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sqrt");
        assertThatThrownBy(() -> StandardBuiltInFunctions.validateGroup(BuiltInFunctionGroup.MATH, missingAbs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("abs");

        StandardBuiltInFunctions.validateGroup(BuiltInFunctionGroup.MATH, completeMathGroup);
    }

    @Test
    @DisplayName("custom boundary coercion keeps assertion conversions foldable")
    void customBoundaryCoercionKeepsAssertionConversionsFoldable() throws Throwable {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService())
                .build();
        FunctionDescriptor asNumber = descriptor(environment.functions(), "asNumber", List.of(ScalarType.STRING));

        assertThat(asNumber.pure()).isTrue();
        assertThat(asNumber.foldable()).isTrue();
        assertThat(asNumber.implementationHandle().invoke("points:12"))
                .isEqualTo(new BigDecimal("12"));
    }

    @Test
    @DisplayName("custom boundary coercion derives profile metadata from the service")
    void customBoundaryCoercionDerivesProfileMetadataFromTheService() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService())
                .build();

        assertThat(environment.conversionProfileIdentity()).isEqualTo("test.prefixed-number");
        assertThat(environment.boundaryCoercion().profileHash()).isEqualTo("test.prefixed-number-hash");
    }

    @Test
    @DisplayName("assertion functions use configured boundary coercion")
    void assertionFunctionsUseConfiguredBoundaryCoercion() throws Throwable {
        FunctionCatalog functions = ExpressionEnvironment.standard().functions();

        assertAssertion(functions, "asNumber", ScalarType.STRING, ScalarType.NUMBER, "12.50", new BigDecimal("12.50"));
        assertAssertion(functions, "asText", ScalarType.NUMBER, ScalarType.STRING, BigDecimal.valueOf(7), "7");
        assertAssertion(functions, "asBool", ScalarType.STRING, ScalarType.BOOLEAN, "true", true);
        assertAssertion(functions, "asDate", ScalarType.STRING, ScalarType.DATE, "2026-07-09", LocalDate.of(2026, 7, 9));
        assertAssertion(functions, "asTime", ScalarType.STRING, ScalarType.TIME, "10:15:30", LocalTime.of(10, 15, 30));
        assertAssertion(functions, "asDateTime", ScalarType.STRING, ScalarType.DATETIME, "2026-07-09T10:15:30",
                LocalDateTime.of(2026, 7, 9, 10, 15, 30));
    }

    @Test
    @DisplayName("assertion functions expose only known scalar source contracts")
    void assertionFunctionsExposeOnlyKnownScalarSourceContracts() {
        FunctionCatalog functions = ExpressionEnvironment.standard().functions();
        Set<ExpressionType> knownScalarTypes = Set.of(
                ScalarType.NUMBER,
                ScalarType.BOOLEAN,
                ScalarType.STRING,
                ScalarType.DATE,
                ScalarType.TIME,
                ScalarType.DATETIME);
        Set<String> assertionNames = Set.of(
                "asNumber", "asText", "asBool", "asDate", "asTime", "asDateTime");

        assertThat(functions.values().stream()
                .filter(descriptor -> assertionNames.contains(descriptor.languageName())))
                .allSatisfy(descriptor -> {
                    assertThat(descriptor.parameterTypes()).hasSize(1);
                    assertThat(descriptor.parameterTypes().getFirst()).isIn(knownScalarTypes);
                    assertThat(descriptor.returnType()).isIn(knownScalarTypes);
                });
        assertThat(functions.values())
                .extracting(FunctionDescriptor::languageName)
                .doesNotContain("asVector");
    }

    private static Object invoke(
            FunctionCatalog functions,
            String languageName,
            List<ExpressionType> parameterTypes,
            Object... arguments) throws Throwable {
        FunctionDescriptor descriptor = descriptor(functions, languageName, parameterTypes);
        assertThat(descriptor.pure()).isTrue();
        assertThat(descriptor.foldable()).isTrue();
        return descriptor.implementationHandle().invokeWithArguments(arguments);
    }

    private static void assertAssertion(
            FunctionCatalog functions,
            String languageName,
            ExpressionType parameterType,
            ExpressionType returnType,
            Object input,
            Object expected) throws Throwable {
        FunctionDescriptor descriptor = descriptor(functions, languageName, List.of(parameterType));

        assertThat(descriptor.pure()).isTrue();
        assertThat(descriptor.foldable()).isTrue();
        assertThat(descriptor.returnType()).isEqualTo(returnType);
        assertThat(descriptor.implementationHandle().invoke(input)).isEqualTo(expected);
    }

    private static FunctionDescriptor descriptor(
            FunctionCatalog functions,
            String languageName,
            List<ExpressionType> parameterTypes) {
        return functions.find(new FunctionSignature(languageName, parameterTypes)).orElseThrow();
    }

    private static List<FunctionDescriptor> standardGroupDescriptors(BuiltInFunctionGroup group) {
        return ExpressionEnvironment.standard().functions().values().stream()
                .filter(descriptor -> group.languageNames().contains(descriptor.languageName()))
                .toList();
    }

    private static List<FunctionDescriptor> descriptorsExcept(
            List<FunctionDescriptor> descriptors,
            String... excludedNames) {
        Set<String> excluded = Set.of(excludedNames);
        return descriptors.stream()
                .filter(descriptor -> !excluded.contains(descriptor.languageName()))
                .toList();
    }
}
