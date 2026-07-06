package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandardBuiltInFunctionTest {

    @Test
    @DisplayName("standard environment includes every official built-in group without user registration")
    void standardEnvironmentIncludesEveryOfficialBuiltInGroupWithoutUserRegistration() {
        FunctionCatalog catalog = ExpressionEnvironment.standard().functionCatalog();
        Set<String> registeredNames = functionNames(catalog.functions());

        assertThat(catalog.size()).isPositive();
        for (StandardFunctionGroup group : StandardFunctionGroup.values()) {
            assertThat(registeredNames)
                    .as(group + " required functions")
                    .containsAll(group.requiredFunctionNames());
        }
    }

    @Test
    @DisplayName("math built-ins include executable abs and sqrt functions")
    void mathBuiltInsIncludeExecutableAbsAndSqrtFunctions() throws Throwable {
        FunctionCatalog catalog = ExpressionEnvironment.standard().functionCatalog();

        FunctionDescriptor abs = required(catalog, "abs", ScalarType.NUMBER);
        FunctionDescriptor sqrt = required(catalog, "sqrt", ScalarType.NUMBER);

        assertThat(abs.implementationHandle().invokeWithArguments(new BigDecimal("-7.25")))
                .isEqualTo(new BigDecimal("7.25"));
        assertThat((BigDecimal) sqrt.implementationHandle().invokeWithArguments(new BigDecimal("9")))
                .isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    @DisplayName("representative behavior is covered for every non-assertion built-in group")
    void representativeBehaviorIsCoveredForEveryNonAssertionBuiltInGroup() throws Throwable {
        FunctionCatalog catalog = ExpressionEnvironment.standard().functionCatalog();

        assertThat((BigDecimal) required(catalog, "sin", ScalarType.NUMBER)
                .implementationHandle()
                .invokeWithArguments(BigDecimal.ZERO))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(required(catalog, "trim", ScalarType.STRING)
                .implementationHandle()
                .invokeWithArguments(" text "))
                .isEqualTo("text");
        assertThat(required(catalog, "daysBetween", UnknownType.INSTANCE, UnknownType.INSTANCE)
                .implementationHandle()
                .invokeWithArguments(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 8)))
                .isEqualTo(new BigDecimal("2"));
        assertThat(required(catalog, "max", new VectorType(UnknownType.INSTANCE))
                .implementationHandle()
                .invokeWithArguments((Object) new String[] {"b", "a"}))
                .isEqualTo("b");
        assertThat((BigDecimal) required(catalog, "npv", ScalarType.NUMBER, new VectorType(ScalarType.NUMBER))
                .implementationHandle()
                .invokeWithArguments(new BigDecimal("0.10"), (Object) new BigDecimal[] {new BigDecimal("110")}))
                .isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("declared math groups must include abs and sqrt")
    void declaredMathGroupsMustIncludeAbsAndSqrt() {
        FunctionDescriptor onlySqrt = dummyDescriptor("sqrt", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor onlyAbs = dummyDescriptor("abs", ScalarType.NUMBER, ScalarType.NUMBER);

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .standardFunctionGroup(StandardFunctionGroup.MATH, List.of(onlySqrt))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MATH")
                .hasMessageContaining("abs");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .standardFunctionGroup(StandardFunctionGroup.MATH, List.of(onlyAbs))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MATH")
                .hasMessageContaining("sqrt");
    }

    @Test
    @DisplayName("each official built-in group is registered as a complete validated set")
    void eachOfficialBuiltInGroupIsRegisteredAsCompleteValidatedSet() {
        for (StandardFunctionGroup group : StandardFunctionGroup.values()) {
            assertThatThrownBy(() -> ExpressionEnvironment.builder()
                    .withoutStandardFunctions()
                    .standardFunctionGroup(group, List.of())
                    .build())
                    .as(group + " rejects partial registration")
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(group.name());
        }
    }

    @Test
    @DisplayName("standard group validation requires complete signatures, not only function names")
    void standardGroupValidationRequiresCompleteSignaturesNotOnlyFunctionNames() {
        List<FunctionDescriptor> wrongContracts = StandardFunctionGroup.MATH.requiredFunctionNames().stream()
                .map(name -> dummyDescriptor(name, UnknownType.INSTANCE, UnknownType.INSTANCE))
                .toList();

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .standardFunctionGroup(StandardFunctionGroup.MATH, wrongContracts)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MATH")
                .hasMessageContaining("foldable=true");
    }

    @Test
    @DisplayName("assertion functions are present, pure, foldable, and convert constants")
    void assertionFunctionsArePresentPureFoldableAndConvertConstants() throws Throwable {
        FunctionCatalog catalog = ExpressionEnvironment.standard().functionCatalog();

        assertAssertion(catalog, "asNumber", ScalarType.NUMBER, "12.50", new BigDecimal("12.50"));
        assertAssertion(catalog, "asText", ScalarType.STRING, "already text", "already text");
        assertAssertion(catalog, "asBool", ScalarType.BOOLEAN, "true", Boolean.TRUE);
        assertAssertion(catalog, "asDate", ScalarType.DATE, "2026-07-06", LocalDate.of(2026, 7, 6));
        assertAssertion(catalog, "asTime", ScalarType.TIME, "10:15:30", LocalTime.of(10, 15, 30));
        assertAssertion(
                catalog,
                "asDateTime",
                ScalarType.DATETIME,
                "2026-07-06T10:15:30",
                LocalDateTime.of(2026, 7, 6, 10, 15, 30));

        FunctionDescriptor asVector = required(catalog, "asVector", UnknownType.INSTANCE);
        assertThat(asVector.returnType()).isEqualTo(new VectorType(UnknownType.INSTANCE));
        assertThat(asVector.isPure()).isTrue();
        assertThat(asVector.isFoldable()).isTrue();
    }

    @Test
    @DisplayName("assertion functions are not advertised as pure or foldable for non-deterministic conversion profiles")
    void assertionFunctionsAreNotPureOrFoldableForNonDeterministicConversionProfiles() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .boundaryCoercion("custom", new RejectingConversionService())
                .build();

        FunctionDescriptor asNumber = required(environment.functionCatalog(), "asNumber", UnknownType.INSTANCE);

        assertThat(asNumber.isPure()).isFalse();
        assertThat(asNumber.isFoldable()).isFalse();
    }

    @Test
    @DisplayName("asVector accepts vectors collections and arrays with materialization limit enforcement")
    void asVectorAcceptsVectorsCollectionsAndArraysWithMaterializationLimitEnforcement() throws Throwable {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .materializationLimit(2)
                .build();
        FunctionDescriptor asVector = required(environment.functionCatalog(), "asVector", UnknownType.INSTANCE);

        assertThat(asVector.implementationHandle().invokeWithArguments((Object) List.of("a", "b")))
                .isEqualTo(List.of("a", "b"));
        assertThat(asVector.implementationHandle().invokeWithArguments((Object) Set.of("a", "b")))
                .asList()
                .containsExactlyInAnyOrder("a", "b");
        assertThat(asVector.implementationHandle().invokeWithArguments((Object) new String[] {"a", "b"}))
                .isEqualTo(List.of("a", "b"));

        assertThatThrownBy(() -> asVector.implementationHandle().invokeWithArguments((Object) List.of("a", "b", "c")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("materialization");
        assertThatThrownBy(() -> asVector.implementationHandle().invokeWithArguments((Object) Map.of("a", "b")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("map")
                .hasMessageContaining("ambiguous");
    }

    private static Set<String> functionNames(Collection<FunctionDescriptor> descriptors) {
        return descriptors.stream()
                .map(FunctionDescriptor::languageName)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static void assertAssertion(
            FunctionCatalog catalog,
            String languageName,
            ExpressionType returnType,
            Object input,
            Object expected) throws Throwable {
        FunctionDescriptor descriptor = required(catalog, languageName, UnknownType.INSTANCE);

        assertThat(descriptor.returnType()).isEqualTo(returnType);
        assertThat(descriptor.isPure()).isTrue();
        assertThat(descriptor.isFoldable()).isTrue();
        assertThat(descriptor.implementationHandle().invokeWithArguments(input)).isEqualTo(expected);
    }

    private static FunctionDescriptor required(
            FunctionCatalog catalog,
            String languageName,
            ExpressionType... parameterTypes) {
        return catalog.find(FunctionSignature.of(languageName, List.of(parameterTypes)))
                .orElseThrow(() -> new AssertionError("missing function: " + languageName));
    }

    private static FunctionDescriptor dummyDescriptor(
            String languageName,
            ExpressionType returnType,
            ExpressionType... parameterTypes) {
        return FunctionDescriptor.builder(languageName)
                .parameterTypes(List.of(parameterTypes))
                .returnType(returnType)
                .implementationHandle(dummyHandle(parameterTypes.length), "test:" + languageName)
                .pure()
                .foldable()
                .build();
    }

    private static MethodHandle dummyHandle(int arity) {
        return MethodHandles.dropArguments(
                MethodHandles.constant(Object.class, null),
                0,
                java.util.Collections.nCopies(arity, Object.class));
    }

    private static final class RejectingConversionService implements DataConversionService {

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return false;
        }

        @Override
        public <S, T> T convert(S source, Class<T> targetType) {
            throw new IllegalArgumentException("unsupported conversion");
        }
    }
}
