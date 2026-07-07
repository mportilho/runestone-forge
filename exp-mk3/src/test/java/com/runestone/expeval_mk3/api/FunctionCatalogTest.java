package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionCatalogTest {

    @Test
    @DisplayName("function descriptors carry language signature, implementation metadata, and purity flags")
    void functionDescriptorsCarryLanguageSignatureImplementationMetadataAndPurityFlags() throws ReflectiveOperationException {
        Method method = BigDecimal.class.getMethod("add", BigDecimal.class);

        FunctionDescriptor descriptor = FunctionDescriptor.builder("sum")
                .parameterTypes(List.of(ScalarType.NUMBER, ScalarType.NUMBER))
                .returnType(ScalarType.NUMBER)
                .implementationMethod(method)
                .pure()
                .foldable()
                .build();

        assertThat(descriptor.languageName()).isEqualTo("sum");
        assertThat(descriptor.arity()).isEqualTo(2);
        assertThat(descriptor.parameterTypes()).containsExactly(ScalarType.NUMBER, ScalarType.NUMBER);
        assertThat(descriptor.returnType()).isEqualTo(ScalarType.NUMBER);
        assertThat(descriptor.signature()).isEqualTo(FunctionSignature.of(
                "sum",
                List.of(ScalarType.NUMBER, ScalarType.NUMBER)));
        assertThat(descriptor.implementationHandle().type())
                .isEqualTo(MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class));
        assertThat(descriptor.implementationDescription()).contains("BigDecimal.add");
        assertThat(descriptor.isPure()).isTrue();
        assertThat(descriptor.isFoldable()).isTrue();
    }

    @Test
    @DisplayName("foldable function descriptors require pure functions")
    void foldableFunctionDescriptorsRequirePureFunctions() {
        assertThatThrownBy(() -> FunctionDescriptor.builder("now")
                .returnType(ScalarType.DATETIME)
                .implementationHandle(zeroArgumentHandle("now"), "test:now")
                .foldable()
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foldable")
                .hasMessageContaining("pure");
    }

    @Test
    @DisplayName("function identity ignores return type and Java implementation details")
    void functionIdentityIgnoresReturnTypeAndJavaImplementationDetails() {
        FunctionDescriptor numberReturn = pureDescriptor("same", ScalarType.NUMBER, ScalarType.STRING);
        FunctionDescriptor stringReturn = pureDescriptor("same", ScalarType.STRING, ScalarType.STRING);

        assertThat(numberReturn.signature()).isEqualTo(stringReturn.signature());
        assertThatThrownBy(() -> FunctionCatalog.builder()
                .register(numberReturn)
                .register(stringReturn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same")
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("duplicate signatures are rejected unless the replacement API is used")
    void duplicateSignaturesAreRejectedUnlessReplacementApiIsUsed() {
        FunctionDescriptor original = pureDescriptor("replaceMe", ScalarType.NUMBER, ScalarType.STRING);
        FunctionDescriptor replacement = pureDescriptor("replaceMe", ScalarType.STRING, ScalarType.STRING);

        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(original)
                .replace(replacement)
                .build();

        assertThat(catalog.find(original.signature())).contains(replacement);
        assertThat(catalog.functions()).containsExactly(replacement);
    }

    @Test
    @DisplayName("exact overload matches are resolved before boundary coercion fallback")
    void exactOverloadMatchesAreResolvedBeforeBoundaryCoercionFallback() {
        FunctionDescriptor exact = pureDescriptor("parse", ScalarType.STRING, ScalarType.STRING);
        FunctionDescriptor coercibleNumber = pureDescriptor("parse", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor coercibleDate = pureDescriptor("parse", ScalarType.DATE, ScalarType.DATE);
        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(coercibleNumber)
                .register(coercibleDate)
                .register(exact)
                .build();

        FunctionResolution resolution = catalog.resolve(
                "parse",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());

        assertThat(resolution.kind()).isEqualTo(FunctionResolution.Kind.EXACT_MATCH);
        assertThat(resolution.descriptor()).contains(exact);
        assertThat(resolution.usesBoundaryCoercion()).isFalse();
        assertThat(resolution.ambiguousCandidates()).isEmpty();
    }

    @Test
    @DisplayName("boundary coercion fallback resolves only when exactly one overload is coercible")
    void boundaryCoercionFallbackResolvesOnlyWhenExactlyOneOverloadIsCoercible() {
        FunctionDescriptor numberTarget = pureDescriptor("asNumber", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(numberTarget)
                .register(pureDescriptor("asNumber", ScalarType.NUMBER, new ObjectType("Customer")))
                .build();

        FunctionResolution resolution = catalog.resolve(
                "asNumber",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());

        assertThat(resolution.kind()).isEqualTo(FunctionResolution.Kind.BOUNDARY_COERCION_MATCH);
        assertThat(resolution.descriptor()).contains(numberTarget);
        assertThat(resolution.usesBoundaryCoercion()).isTrue();
    }

    @Test
    @DisplayName("multiple boundary-coercible overloads produce deterministic ambiguity")
    void multipleBoundaryCoercibleOverloadsProduceDeterministicAmbiguity() {
        FunctionDescriptor dateTarget = pureDescriptor("parse", ScalarType.DATE, ScalarType.DATE);
        FunctionDescriptor numberTarget = pureDescriptor("parse", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionCatalog firstOrder = FunctionCatalog.builder()
                .register(dateTarget)
                .register(numberTarget)
                .build();
        FunctionCatalog secondOrder = FunctionCatalog.builder()
                .register(numberTarget)
                .register(dateTarget)
                .build();

        FunctionResolution firstResolution = firstOrder.resolve(
                "parse",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());
        FunctionResolution secondResolution = secondOrder.resolve(
                "parse",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());

        assertThat(firstResolution.kind()).isEqualTo(FunctionResolution.Kind.AMBIGUOUS);
        assertThat(secondResolution.kind()).isEqualTo(FunctionResolution.Kind.AMBIGUOUS);
        assertThat(firstResolution.ambiguousCandidates())
                .containsExactlyElementsOf(secondResolution.ambiguousCandidates())
                .containsExactly(dateTarget, numberTarget);
        assertThat(firstResolution.descriptor()).isEmpty();
        assertThat(firstResolution.usesBoundaryCoercion()).isTrue();
    }

    @Test
    @DisplayName("lookup returns no match when neither exact nor coercion fallback can bind")
    void lookupReturnsNoMatchWhenNeitherExactNorCoercionFallbackCanBind() {
        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(pureDescriptor("year", ScalarType.NUMBER, ScalarType.DATE))
                .build();

        FunctionResolution resolution = catalog.resolve(
                "year",
                List.of(ScalarType.BOOLEAN),
                BoundaryCoercion.standard());

        assertThat(resolution.kind()).isEqualTo(FunctionResolution.Kind.NO_MATCH);
        assertThat(resolution.descriptor()).isEmpty();
        assertThat(resolution.ambiguousCandidates()).isEmpty();
    }

    @Test
    @DisplayName("semantic resolution treats unknown arguments as call-site checks without concrete coercion")
    void semanticResolutionTreatsUnknownArgumentsAsCallSiteChecksWithoutConcreteCoercion() {
        FunctionDescriptor onlyNumber = pureDescriptor("score", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(onlyNumber)
                .build();

        FunctionResolution unknownResolution = catalog.resolveSemantic("score", List.of(UnknownType.INSTANCE));
        FunctionResolution concreteMismatch = catalog.resolveSemantic("score", List.of(ScalarType.STRING));

        assertThat(unknownResolution.kind()).isEqualTo(FunctionResolution.Kind.UNKNOWN_ARGUMENT_MATCH);
        assertThat(unknownResolution.descriptor()).contains(onlyNumber);
        assertThat(concreteMismatch.kind()).isEqualTo(FunctionResolution.Kind.NO_MATCH);
    }

    @Test
    @DisplayName("semantic resolution prefers explicit unknown-accepting signatures over unknown overload ambiguity")
    void semanticResolutionPrefersExplicitUnknownAcceptingSignaturesOverUnknownOverloadAmbiguity() {
        FunctionDescriptor numberOverload = pureDescriptor("refine", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor textOverload = pureDescriptor("refine", ScalarType.STRING, ScalarType.STRING);
        FunctionDescriptor unknownAccepting = pureDescriptor("refine", ScalarType.BOOLEAN, UnknownType.INSTANCE);
        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(numberOverload)
                .register(textOverload)
                .register(unknownAccepting)
                .build();

        FunctionResolution resolution = catalog.resolveSemantic("refine", List.of(UnknownType.INSTANCE));

        assertThat(resolution.kind()).isEqualTo(FunctionResolution.Kind.EXACT_MATCH);
        assertThat(resolution.descriptor()).contains(unknownAccepting);
    }

    @Test
    @DisplayName("semantic unknown overload ambiguity is not reported as boundary coercion")
    void semanticUnknownOverloadAmbiguityIsNotReportedAsBoundaryCoercion() {
        FunctionCatalog catalog = FunctionCatalog.builder()
                .register(pureDescriptor("score", ScalarType.NUMBER, ScalarType.NUMBER))
                .register(pureDescriptor("score", ScalarType.NUMBER, ScalarType.STRING))
                .build();

        FunctionResolution resolution = catalog.resolveSemantic("score", List.of(UnknownType.INSTANCE));

        assertThat(resolution.kind()).isEqualTo(FunctionResolution.Kind.AMBIGUOUS);
        assertThat(resolution.usesBoundaryCoercion()).isFalse();
    }

    private static FunctionDescriptor pureDescriptor(
            String languageName,
            ExpressionType returnType,
            ExpressionType... parameterTypes) {
        FunctionDescriptor.Builder builder = FunctionDescriptor.builder(languageName)
                .parameterTypes(List.of(parameterTypes))
                .returnType(returnType)
                .implementationHandle(argumentHandle(languageName, parameterTypes.length), "test:" + languageName);
        return builder.pure().build();
    }

    private static MethodHandle zeroArgumentHandle(String languageName) {
        return MethodHandles.constant(Object.class, languageName);
    }

    private static MethodHandle argumentHandle(String languageName, int arity) {
        return MethodHandles.dropArguments(
                zeroArgumentHandle(languageName),
                0,
                Collections.nCopies(arity, Object.class));
    }
}
