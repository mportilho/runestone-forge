package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionCatalogTest {

    @Test
    @DisplayName("function descriptors expose language signature and implementation metadata")
    void functionDescriptorsExposeLanguageSignatureAndImplementationMetadata() throws NoSuchMethodException {
        FunctionDescriptor descriptor = descriptor(
                "identity",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);

        assertThat(descriptor.languageName()).isEqualTo("identity");
        assertThat(descriptor.arity()).isOne();
        assertThat(descriptor.parameterTypes()).containsExactly(ScalarType.NUMBER);
        assertThat(descriptor.returnType()).isEqualTo(ScalarType.NUMBER);
        assertThat(descriptor.implementationHandle()).isNotNull();
        assertThat(descriptor.implementationMetadata().kind()).isEqualTo("static-method");
        assertThat(descriptor.implementationMetadata().owner()).endsWith("FunctionCatalogTest$TestFunctions");
        assertThat(descriptor.implementationMetadata().memberName()).isEqualTo("numberIdentity");
        assertThat(descriptor.implementationMetadata().methodType())
                .isEqualTo("(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;");
        assertThat(descriptor.pure()).isTrue();
        assertThat(descriptor.foldable()).isTrue();
        assertThat(descriptor.signature())
                .isEqualTo(new FunctionSignature("identity", List.of(ScalarType.NUMBER)));
    }

    @Test
    @DisplayName("reflected function importer is a public extension API")
    void reflectedFunctionImporterIsAPublicExtensionApi() {
        assertThat(Modifier.isPublic(ReflectedFunctionImporter.class.getModifiers())).isTrue();
        assertThat(Modifier.isPublic(ReflectedFunctionImporter.ImportPlan.class.getModifiers())).isTrue();
        assertThat(Modifier.isPublic(ReflectedFunctionImporter.Selection.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("foldable function purity is represented without an impure foldable state")
    void foldableFunctionPurityIsRepresentedWithoutImpureFoldableState() {
        assertThat(FunctionPurity.FOLDABLE.pure()).isTrue();
        assertThat(FunctionPurity.FOLDABLE.foldable()).isTrue();
        assertThat(FunctionPurity.PURE.pure()).isTrue();
        assertThat(FunctionPurity.PURE.foldable()).isFalse();
        assertThat(FunctionPurity.IMPURE.pure()).isFalse();
        assertThat(FunctionPurity.IMPURE.foldable()).isFalse();
    }

    @Test
    @DisplayName("function identity ignores return type and reflection metadata")
    void functionIdentityIgnoresReturnTypeAndReflectionMetadata() throws NoSuchMethodException {
        FunctionDescriptor first = descriptor(
                "same",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);
        FunctionDescriptor sameSignatureDifferentReturnAndMethod = descriptor(
                "same",
                "numberAsText",
                List.of(ScalarType.NUMBER),
                ScalarType.STRING,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);

        assertThat(first.signature()).isEqualTo(sameSignatureDifferentReturnAndMethod.signature());
        assertThatThrownBy(() -> FunctionCatalog.builder()
                .register(first)
                .register(sameSignatureDifferentReturnAndMethod))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        FunctionCatalog replaced = FunctionCatalog.builder()
                .register(first)
                .replace(sameSignatureDifferentReturnAndMethod)
                .build();

        FunctionLookupResult lookup = replaced.resolve(
                "same",
                List.of(ScalarType.NUMBER));
        assertThat(lookup.status()).isEqualTo(FunctionLookupResult.Status.EXACT_MATCH);
        assertThat(lookup.descriptor()).contains(sameSignatureDifferentReturnAndMethod);
    }

    @Test
    @DisplayName("exact overload lookup is deterministic and wins before coercion fallback")
    void exactOverloadLookupIsDeterministicAndWinsBeforeCoercionFallback() throws NoSuchMethodException {
        FunctionDescriptor text = descriptor(
                "value",
                "textIdentity",
                List.of(ScalarType.STRING),
                ScalarType.STRING,
                FunctionPurity.FOLDABLE,
                String.class);
        FunctionDescriptor number = descriptor(
                "value",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);

        FunctionCatalog firstOrder = FunctionCatalog.builder().register(text).register(number).build();
        FunctionCatalog secondOrder = FunctionCatalog.builder().register(number).register(text).build();

        FunctionLookupResult firstLookup = firstOrder.resolve(
                "value",
                List.of(ScalarType.STRING));
        FunctionLookupResult secondLookup = secondOrder.resolve(
                "value",
                List.of(ScalarType.STRING));

        assertThat(firstLookup.status()).isEqualTo(FunctionLookupResult.Status.EXACT_MATCH);
        assertThat(firstLookup.descriptor()).contains(text);
        assertThat(secondLookup.status()).isEqualTo(FunctionLookupResult.Status.EXACT_MATCH);
        assertThat(secondLookup.descriptor()).contains(text);
    }

    @Test
    @DisplayName("function lookup does not coerce known expression argument types")
    void functionLookupDoesNotCoerceKnownExpressionArgumentTypes() throws NoSuchMethodException {
        FunctionDescriptor number = descriptor(
                "parse",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);
        FunctionDescriptor booleanValue = descriptor(
                "parse",
                "booleanIdentity",
                List.of(ScalarType.BOOLEAN),
                ScalarType.BOOLEAN,
                FunctionPurity.FOLDABLE,
                Boolean.class);

        FunctionCatalog uniqueCatalog = FunctionCatalog.builder().register(number).build();
        FunctionLookupResult mismatched = uniqueCatalog.resolve(
                "parse",
                List.of(ScalarType.STRING));

        assertThat(mismatched.status()).isEqualTo(FunctionLookupResult.Status.NOT_FOUND);
        assertThat(mismatched.descriptor()).isEmpty();

        FunctionCatalog ambiguousCatalog = FunctionCatalog.builder().register(number).register(booleanValue).build();
        FunctionLookupResult mismatchedOverloads = ambiguousCatalog.resolve(
                "parse",
                List.of(ScalarType.STRING));

        assertThat(mismatchedOverloads.status()).isEqualTo(FunctionLookupResult.Status.NOT_FOUND);
        assertThat(mismatchedOverloads.descriptor()).isEmpty();
        assertThat(mismatchedOverloads.candidates()).isEmpty();
    }

    @Test
    @DisplayName("function catalog order is deterministic")
    void functionCatalogOrderIsDeterministic() throws NoSuchMethodException {
        FunctionDescriptor text = descriptor(
                "value",
                "textIdentity",
                List.of(ScalarType.STRING),
                ScalarType.STRING,
                FunctionPurity.FOLDABLE,
                String.class);
        FunctionDescriptor number = descriptor(
                "value",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);

        FunctionCatalog first = FunctionCatalog.builder().register(text).register(number).build();
        FunctionCatalog second = FunctionCatalog.builder().register(number).register(text).build();

        assertThat(first.values()).extracting(FunctionDescriptor::signature)
                .containsExactlyElementsOf(second.values().stream().map(FunctionDescriptor::signature).toList());
    }

    @Test
    @DisplayName("duplicate diagnostics report both origins independently of registration order")
    void duplicateDiagnosticsReportBothOriginsIndependentlyOfRegistrationOrder() throws NoSuchMethodException {
        FunctionDescriptor first = descriptor(
                "same",
                "numberIdentity",
                List.of(ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);
        FunctionDescriptor second = descriptor(
                "same",
                "numberAsText",
                List.of(ScalarType.NUMBER),
                ScalarType.STRING,
                FunctionPurity.FOLDABLE,
                BigDecimal.class);

        String firstOrder = duplicateMessage(first, second);
        String secondOrder = duplicateMessage(second, first);

        assertThat(firstOrder)
                .isEqualTo(secondOrder)
                .contains("numberIdentity")
                .contains("numberAsText");
    }

    private static String duplicateMessage(FunctionDescriptor first, FunctionDescriptor second) {
        try {
            FunctionCatalog.builder().register(first).register(second);
            throw new AssertionError("duplicate registration should fail");
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
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

    static final class TestFunctions {

        static BigDecimal numberIdentity(BigDecimal value) {
            return value;
        }

        static String numberAsText(BigDecimal value) {
            return value.toPlainString();
        }

        static String textIdentity(String value) {
            return value;
        }

        static Boolean booleanIdentity(Boolean value) {
            return value;
        }
    }
}
