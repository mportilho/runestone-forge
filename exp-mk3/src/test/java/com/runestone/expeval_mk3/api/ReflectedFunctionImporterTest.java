package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReflectedFunctionImporterTest {

    @Test
    @DisplayName("imports only directly declared public static provider methods")
    void importsOnlyDirectlyDeclaredPublicStaticProviderMethods() {
        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(StaticProvider.class, FunctionPurity.FOLDABLE)
                .toList();

        assertThat(descriptors)
                .extracting(FunctionDescriptor::signature)
                .containsExactly(
                        signature("addOne", ScalarType.NUMBER),
                        signature("today"));
        assertThat(descriptor(descriptors, "today").returnType()).isEqualTo(ScalarType.DATE);
    }

    @Test
    @DisplayName("imports public instance methods and binds provider instance")
    void importsPublicInstanceMethodsAndBindsProviderInstance() throws Throwable {
        InstanceProvider provider = new InstanceProvider(BigDecimal.TEN);

        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(provider, "tenant-10", FunctionPurity.PURE)
                .toList();

        FunctionDescriptor descriptor = only(descriptors);
        assertThat(descriptor.signature()).isEqualTo(signature("addBase", ScalarType.NUMBER));
        assertThat(descriptor.implementationHandle().type())
                .isEqualTo(MethodType.methodType(BigDecimal.class, BigDecimal.class));
        assertThat(descriptor.pure()).isTrue();
        assertThat(descriptor.foldable()).isFalse();
        assertThat(descriptor.implementationMetadata().stableImplementationId())
                .isEqualTo("instance-method:"
                        + InstanceProvider.class.getName()
                        + "#addBase(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;@provider:9:tenant-10");
        assertThat(descriptor.implementationHandle().invoke(new BigDecimal("5")))
                .isEqualTo(new BigDecimal("15"));
    }

    @Test
    @DisplayName("provider ID makes instance function identity stable and cache safe")
    void providerIdMakesInstanceFunctionIdentityStableAndCacheSafe() {
        FunctionDescriptor first = only(ReflectedFunctionImporter
                .importAll(new InstanceProvider(BigDecimal.TEN), "tenant-10", FunctionPurity.PURE)
                .toList());
        FunctionDescriptor equivalent = only(ReflectedFunctionImporter
                .importAll(new InstanceProvider(BigDecimal.TEN), "tenant-10", FunctionPurity.PURE)
                .toList());
        FunctionDescriptor differentProvider = only(ReflectedFunctionImporter
                .importAll(new InstanceProvider(BigDecimal.ONE), "tenant-1", FunctionPurity.PURE)
                .toList());

        ExpressionEnvironment firstEnvironment = ExpressionEnvironment.builder().function(first).build();
        ExpressionEnvironment equivalentEnvironment = ExpressionEnvironment.builder().function(equivalent).build();
        ExpressionEnvironment differentProviderEnvironment = ExpressionEnvironment.builder()
                .function(differentProvider)
                .build();

        assertThat(first.implementationMetadata()).isEqualTo(equivalent.implementationMetadata());
        assertThat(firstEnvironment.environmentId()).isEqualTo(equivalentEnvironment.environmentId());
        assertThat(firstEnvironment.environmentId()).isNotEqualTo(differentProviderEnvironment.environmentId());
    }

    @Test
    @DisplayName("explicit exposure type imports declared interface methods bound to instance")
    void explicitExposureTypeImportsDeclaredInterfaceMethodsBoundToInstance() throws Throwable {
        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(ExposedContract.class, new ContractProvider(), "contract-v1", FunctionPurity.FOLDABLE)
                .toList();

        assertThat(descriptors)
                .extracting(FunctionDescriptor::signature)
                .containsExactly(
                        signature("contract", ScalarType.STRING),
                        signature("defaultContract", ScalarType.STRING));
        assertThat(descriptor(descriptors, "defaultContract", ScalarType.STRING)
                .implementationHandle()
                .invoke("stone"))
                .isEqualTo("default:stone");
    }

    @Test
    @DisplayName("rejects invalid exposure instance and requires purity")
    void rejectsInvalidExposureInstanceAndRequiresPurity() {
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(ExposedContract.class, new Object(), "invalid", FunctionPurity.PURE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exposureType");
        assertThatThrownBy(() -> ReflectedFunctionImporter.importAll(StaticProvider.class, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purity");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(new InstanceProvider(BigDecimal.ONE), "instance-1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purity");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(ExposedContract.class, new ContractProvider(), "contract-v1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purity");
        assertThatThrownBy(() -> ReflectedFunctionImporter.importSelected(StaticProvider.class, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purity");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importSelected(new InstanceProvider(BigDecimal.ONE), "instance-1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purity");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importSelected(ExposedContract.class, new ContractProvider(), "contract-v1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purity");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(new InstanceProvider(BigDecimal.ONE), null, FunctionPurity.PURE))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("providerId");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(new InstanceProvider(BigDecimal.ONE), " ", FunctionPurity.PURE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("providerId");
    }

    @Test
    @DisplayName("infers scalar and vector expression types")
    void infersScalarAndVectorExpressionTypes() {
        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(TypeInferenceProvider.class, FunctionPurity.FOLDABLE)
                .toList();

        assertThat(descriptors)
                .extracting(FunctionDescriptor::signature)
                .containsExactly(
                        signature("booleans", ScalarType.BOOLEAN, ScalarType.BOOLEAN),
                        signature("dates", ScalarType.DATE, ScalarType.TIME, ScalarType.DATETIME),
                        signature("number", ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER),
                        signature("numbers", new VectorType(ScalarType.NUMBER), new VectorType(ScalarType.STRING)),
                        signature("text", ScalarType.STRING));
        assertThat(descriptor(descriptors, "numbers", new VectorType(ScalarType.NUMBER), new VectorType(ScalarType.STRING))
                .returnType())
                .isEqualTo(new VectorType(ScalarType.NUMBER));
    }

    @Test
    @DisplayName("rejects unsupported provider method signatures")
    void rejectsUnsupportedProviderMethodSignatures() {
        assertUnsupported(RawListReturnProvider.class, "raw List");
        assertUnsupported(RawCollectionParameterProvider.class, "raw Collection");
        assertUnsupported(CollectionReturnProvider.class, "Collection return");
        assertUnsupported(ArrayProvider.class, "array");
        assertUnsupported(MapProvider.class, "Map");
        assertUnsupported(ObjectProvider.class, "Object");
        assertUnsupported(OptionalProvider.class, "Optional");
        assertUnsupported(VoidProvider.class, "void");
        assertUnsupported(VarargsProvider.class, "varargs");
        assertUnsupported(UnsupportedObjectProvider.class, "unsupported");
        assertUnsupported(WildcardVectorProvider.class, "wildcard");
    }

    @Test
    @DisplayName("adapts primitive and wrapper numeric parameters and returns through BigDecimal")
    void adaptsPrimitiveAndWrapperNumericParametersAndReturnsThroughBigDecimal() throws Throwable {
        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(NumericAdapterProvider.class, FunctionPurity.FOLDABLE)
                .toList();

        assertThat(descriptor(descriptors, "addInt", ScalarType.NUMBER)
                .implementationHandle()
                .invoke(new BigDecimal("41")))
                .isEqualTo(new BigDecimal("42"));
        assertThat(descriptor(descriptors, "addLong", ScalarType.NUMBER)
                .implementationHandle()
                .invoke(new BigDecimal("41")))
                .isEqualTo(new BigDecimal("42"));
        assertThat(descriptor(descriptors, "half", ScalarType.NUMBER)
                .implementationHandle()
                .invoke(new BigDecimal("21")))
                .isEqualTo(new BigDecimal("42.0"));
        assertThat(descriptor(descriptors, "identityDouble", ScalarType.NUMBER)
                .implementationHandle()
                .invoke(new BigDecimal("1.25")))
                .isEqualTo(new BigDecimal("1.25"));
        assertThatThrownBy(() -> descriptor(descriptors, "addInt", ScalarType.NUMBER)
                .implementationHandle()
                .invoke(new BigDecimal("1.5")))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("applies renames and rejects missing or conflicting renames")
    void appliesRenamesAndRejectsMissingOrConflictingRenames() {
        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(RenameProvider.class, FunctionPurity.FOLDABLE)
                .rename("maxNumber", "max")
                .rename("maxText", "max")
                .toList();

        assertThat(descriptors)
                .extracting(FunctionDescriptor::signature)
                .containsExactly(
                        signature("max", new VectorType(ScalarType.NUMBER)),
                        signature("max", new VectorType(ScalarType.STRING)));

        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(RenameProvider.class, FunctionPurity.FOLDABLE)
                .rename("missing", "x")
                .toList())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(RenameProvider.class, FunctionPurity.FOLDABLE)
                .rename("maxNumber", "max")
                .rename("maxNumber", "greatest")
                .toList())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxNumber");
    }

    @Test
    @DisplayName("fails on duplicate imported function signatures")
    void failsOnDuplicateImportedFunctionSignatures() {
        assertThatThrownBy(() -> ReflectedFunctionImporter
                .importAll(DuplicateProvider.class, FunctionPurity.FOLDABLE)
                .rename("sameTwo", "sameOne")
                .toList())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    @DisplayName("selective import supports all overloads by name and exact Java parameter types")
    void selectiveImportSupportsAllOverloadsByNameAndExactJavaParameterTypes() {
        List<FunctionDescriptor> byName = ReflectedFunctionImporter
                .importSelected(SelectiveProvider.class, FunctionPurity.FOLDABLE)
                .methods("value")
                .toList();
        List<FunctionDescriptor> exact = ReflectedFunctionImporter
                .importSelected(SelectiveProvider.class, FunctionPurity.FOLDABLE)
                .method("value", String.class)
                .toList();

        assertThat(byName)
                .extracting(FunctionDescriptor::signature)
                .containsExactly(
                        signature("value", ScalarType.NUMBER),
                        signature("value", ScalarType.STRING));
        assertThat(exact)
                .extracting(FunctionDescriptor::signature)
                .containsExactly(signature("value", ScalarType.STRING));
    }

    @Test
    @DisplayName("descriptor order is deterministic by language signature and Java method name")
    void descriptorOrderIsDeterministicByLanguageSignatureAndJavaMethodName() {
        List<FunctionDescriptor> descriptors = ReflectedFunctionImporter
                .importAll(OrderProvider.class, FunctionPurity.FOLDABLE)
                .rename("aText", "a")
                .rename("aNumber", "a")
                .toList();

        assertThat(descriptors)
                .extracting(descriptor -> descriptor.signature().canonical())
                .containsExactly(
                        signature("a", ScalarType.NUMBER).canonical(),
                        signature("a", ScalarType.STRING).canonical(),
                        signature("b").canonical());
    }

    private static void assertUnsupported(Class<?> providerClass, String expectedMessage) {
        assertThatThrownBy(() -> ReflectedFunctionImporter.importAll(providerClass, FunctionPurity.FOLDABLE).toList())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    private static FunctionDescriptor only(List<FunctionDescriptor> descriptors) {
        assertThat(descriptors).hasSize(1);
        return descriptors.getFirst();
    }

    private static FunctionDescriptor descriptor(
            List<FunctionDescriptor> descriptors,
            String languageName,
            ExpressionType... parameterTypes) {
        FunctionSignature signature = signature(languageName, parameterTypes);
        return descriptors.stream()
                .filter(candidate -> candidate.signature().equals(signature))
                .findFirst()
                .orElseThrow();
    }

    private static FunctionSignature signature(String languageName, ExpressionType... parameterTypes) {
        return new FunctionSignature(languageName, List.of(parameterTypes));
    }

    static class ParentProvider {
        public static BigDecimal inheritedStatic(BigDecimal value) {
            return value;
        }

        public BigDecimal inheritedInstance(BigDecimal value) {
            return value;
        }
    }

    static final class StaticProvider extends ParentProvider {
        public static BigDecimal addOne(BigDecimal value) {
            return value.add(BigDecimal.ONE);
        }

        public static LocalDate today() {
            return LocalDate.of(2026, 7, 10);
        }

        public BigDecimal instanceViaClass(BigDecimal value) {
            return value;
        }

        static BigDecimal packagePrivate(BigDecimal value) {
            return value;
        }

        protected static BigDecimal protectedMethod(BigDecimal value) {
            return value;
        }

        @SuppressWarnings("unused")
        private static BigDecimal privateMethod(BigDecimal value) {
            return value;
        }
    }

    static final class InstanceProvider {
        private final BigDecimal base;

        private InstanceProvider(BigDecimal base) {
            this.base = base;
        }

        public BigDecimal addBase(BigDecimal value) {
            return base.add(value);
        }

        public static BigDecimal staticViaInstance(BigDecimal value) {
            return value;
        }
    }

    interface ExposedContract {
        String contract(String value);

        default String defaultContract(String value) {
            return "default:" + value;
        }
    }

    static final class ContractProvider implements ExposedContract {
        @Override
        public String contract(String value) {
            return "contract:" + value;
        }

        public String implementationOnly(String value) {
            return value;
        }
    }

    static final class TypeInferenceProvider {
        public static Boolean booleans(boolean first, Boolean second) {
            return first && second;
        }

        public static LocalDateTime dates(LocalDate date, LocalTime time, LocalDateTime dateTime) {
            return dateTime.with(date).with(time);
        }

        public static BigDecimal number(byte first, Integer second, BigDecimal third) {
            return BigDecimal.valueOf(first).add(BigDecimal.valueOf(second)).add(third);
        }

        public static List<BigDecimal> numbers(List<BigDecimal> values, Collection<String> labels) {
            return values.subList(0, Math.min(values.size(), labels.size()));
        }

        public static String text(String value) {
            return value;
        }
    }

    static final class RawListReturnProvider {
        @SuppressWarnings("rawtypes")
        public static List raw() {
            return List.of();
        }
    }

    static final class RawCollectionParameterProvider {
        @SuppressWarnings("rawtypes")
        public static BigDecimal raw(Collection values) {
            return BigDecimal.valueOf(values.size());
        }
    }

    static final class CollectionReturnProvider {
        public static Collection<BigDecimal> values() {
            return List.of(BigDecimal.ONE);
        }
    }

    static final class ArrayProvider {
        public static BigDecimal[] values() {
            return new BigDecimal[]{BigDecimal.ONE};
        }
    }

    static final class MapProvider {
        public static BigDecimal map(Map<String, BigDecimal> values) {
            return values.get("a");
        }
    }

    static final class ObjectProvider {
        public static Object object(Object value) {
            return value;
        }
    }

    static final class OptionalProvider {
        public static Optional<BigDecimal> optional(BigDecimal value) {
            return Optional.of(value);
        }
    }

    static final class VoidProvider {
        public static void nothing(BigDecimal value) {
        }
    }

    static final class VarargsProvider {
        public static BigDecimal varargs(BigDecimal... values) {
            return values[0];
        }
    }

    static final class UnsupportedObjectProvider {
        public static CustomObject custom(CustomObject value) {
            return value;
        }
    }

    static final class WildcardVectorProvider {
        public static String join(List<?> values) {
            return values.toString();
        }
    }

    static final class CustomObject {
    }

    static final class NumericAdapterProvider {
        public static int addInt(int value) {
            return value + 1;
        }

        public static Long addLong(Long value) {
            return value + 1;
        }

        public static float half(float value) {
            return value * 2;
        }

        public static Double identityDouble(Double value) {
            return value;
        }
    }

    static final class RenameProvider {
        public static BigDecimal maxNumber(List<BigDecimal> values) {
            return values.getFirst();
        }

        public static String maxText(List<String> values) {
            return values.getFirst();
        }
    }

    static final class DuplicateProvider {
        public static BigDecimal sameOne(BigDecimal value) {
            return value;
        }

        public static String sameTwo(BigDecimal value) {
            return value.toPlainString();
        }
    }

    static final class SelectiveProvider {
        public static BigDecimal value(BigDecimal value) {
            return value;
        }

        public static String value(String value) {
            return value;
        }

        public static Boolean other(Boolean value) {
            return value;
        }
    }

    static final class OrderProvider {
        public static String aText(String value) {
            return value;
        }

        public static BigDecimal aNumber(BigDecimal value) {
            return value;
        }

        public static BigDecimal b() {
            return BigDecimal.ONE;
        }
    }
}
