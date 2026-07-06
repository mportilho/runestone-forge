package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ExpressionEnvironmentAcceptanceGateTest {

    @Test
    @DisplayName("equivalent complete environments produce the same environment id")
    void equivalentCompleteEnvironmentsProduceSameEnvironmentId() {
        FunctionDescriptor riskScore = pureFoldableDescriptor("riskScore", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor normalizeText = pureFoldableDescriptor("normalizeText", ScalarType.STRING, ScalarType.STRING);
        JavaTypeCatalog.RegisteredJavaType customer = JavaTypeCatalog.registerJavaType(
                JavaTypeCatalogTest.CustomerBean.class,
                "Customer")
                .build();
        JavaTypeCatalog.RegisteredJavaType account = JavaTypeCatalog.registerJavaType(
                JavaTypeCatalogTest.AccountBean.class,
                "Account")
                .build();

        ExpressionEnvironment first = baseBuilder()
                .externalSymbol("amount", ScalarType.NUMBER)
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 6))
                .externalSymbolWithDefault("metadata", new MapType(ScalarType.STRING), Map.of("tier", "gold"))
                .function(riskScore)
                .function(normalizeText)
                .registerJavaType(customer)
                .registerJavaType(account)
                .build();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .conversionProfileId("acceptance-v1")
                .materializationLimit(1_234)
                .maxCurrentItemDepth(7)
                .strictMode(true)
                .transcendentalMathContext(new MathContext(23, RoundingMode.CEILING))
                .mathContext(new MathContext(19, RoundingMode.FLOOR))
                .zoneId(ZoneId.of("Europe/Paris"))
                .numericMode(NumericMode.FAST)
                .registerJavaType(account)
                .registerJavaType(customer)
                .function(normalizeText)
                .function(riskScore)
                .externalSymbolWithDefault("metadata", new MapType(ScalarType.STRING), Map.of("tier", "gold"))
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 6))
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        assertThat(first.environmentId()).isEqualTo(sameContentDifferentOrder.environmentId());
        assertThat(first).isEqualTo(sameContentDifferentOrder);
    }

    @Test
    @DisplayName("environment id changes for every accepted Etapa 3 identity dimension")
    void environmentIdChangesForEveryAcceptedEtapa3IdentityDimension() {
        ExpressionEnvironment baseline = identityDimensionEnvironment(IdentityDimension.BASELINE);

        List<ExpressionEnvironmentId> changedIds = List.of(
                identityDimensionEnvironment(IdentityDimension.SYMBOL_NAME).environmentId(),
                identityDimensionEnvironment(IdentityDimension.SYMBOL_TYPE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.SYMBOL_DEFAULT).environmentId(),
                identityDimensionEnvironment(IdentityDimension.FUNCTION_SIGNATURE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.FUNCTION_RETURN_TYPE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.FUNCTION_IMPLEMENTATION).environmentId(),
                identityDimensionEnvironment(IdentityDimension.FUNCTION_FLAGS).environmentId(),
                identityDimensionEnvironment(IdentityDimension.JAVA_TYPE_OBJECT).environmentId(),
                identityDimensionEnvironment(IdentityDimension.JAVA_TYPE_CLASS).environmentId(),
                identityDimensionEnvironment(IdentityDimension.JAVA_TYPE_EXPOSURE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.NUMERIC_MODE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.TIME_ZONE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.MATH_CONTEXT_PRECISION).environmentId(),
                identityDimensionEnvironment(IdentityDimension.MATH_CONTEXT_ROUNDING).environmentId(),
                identityDimensionEnvironment(IdentityDimension.TRANSCENDENTAL_CONTEXT_PRECISION).environmentId(),
                identityDimensionEnvironment(IdentityDimension.TRANSCENDENTAL_CONTEXT_ROUNDING).environmentId(),
                identityDimensionEnvironment(IdentityDimension.STRICT_MODE).environmentId(),
                identityDimensionEnvironment(IdentityDimension.CURRENT_ITEM_DEPTH).environmentId(),
                identityDimensionEnvironment(IdentityDimension.MATERIALIZATION_LIMIT).environmentId(),
                identityDimensionEnvironment(IdentityDimension.CONVERSION_PROFILE).environmentId());

        assertThat(changedIds)
                .doesNotContain(baseline.environmentId())
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("function catalogs expose overloads in deterministic order")
    void functionCatalogsExposeOverloadsInDeterministicOrder() {
        FunctionCatalog firstOrder = deterministicCatalog(RegistrationOrder.FIRST);
        FunctionCatalog secondOrder = deterministicCatalog(RegistrationOrder.SECOND);

        assertThat(firstOrder.functions()).containsExactlyElementsOf(secondOrder.functions());
    }

    @Test
    @DisplayName("exact function overload matches are deterministic across registration order")
    void exactFunctionOverloadMatchesAreDeterministicAcrossRegistrationOrder() {
        FunctionCatalog firstOrder = deterministicCatalog(RegistrationOrder.FIRST);
        FunctionCatalog secondOrder = deterministicCatalog(RegistrationOrder.SECOND);

        FunctionResolution firstExact = firstOrder.resolve(
                "convert",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());
        FunctionResolution secondExact = secondOrder.resolve(
                "convert",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());

        assertThat(firstExact.kind()).isEqualTo(FunctionResolution.Kind.EXACT_MATCH);
        assertThat(secondExact.kind()).isEqualTo(FunctionResolution.Kind.EXACT_MATCH);
        assertThat(firstExact.descriptor()).isEqualTo(secondExact.descriptor());
    }

    @Test
    @DisplayName("ambiguous function overload matches report candidates consistently")
    void ambiguousFunctionOverloadMatchesReportCandidatesConsistently() {
        FunctionDescriptor numberTarget = pureFoldableDescriptor("parse", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor dateTarget = pureFoldableDescriptor("parse", ScalarType.DATE, ScalarType.DATE);
        FunctionCatalog firstOrder = deterministicCatalog(RegistrationOrder.FIRST);
        FunctionCatalog secondOrder = deterministicCatalog(RegistrationOrder.SECOND);

        FunctionResolution firstAmbiguous = firstOrder.resolve(
                "parse",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());
        FunctionResolution secondAmbiguous = secondOrder.resolve(
                "parse",
                List.of(ScalarType.STRING),
                BoundaryCoercion.standard());

        assertThat(firstAmbiguous.kind()).isEqualTo(FunctionResolution.Kind.AMBIGUOUS);
        assertThat(secondAmbiguous.kind()).isEqualTo(FunctionResolution.Kind.AMBIGUOUS);
        assertThat(firstAmbiguous.ambiguousCandidates())
                .containsExactly(dateTarget, numberTarget)
                .containsExactlyElementsOf(secondAmbiguous.ambiguousCandidates());
    }

    private static FunctionCatalog deterministicCatalog(RegistrationOrder order) {
        FunctionDescriptor exact = pureFoldableDescriptor("convert", ScalarType.STRING, ScalarType.STRING);
        FunctionDescriptor numberTarget = pureFoldableDescriptor("parse", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor dateTarget = pureFoldableDescriptor("parse", ScalarType.DATE, ScalarType.DATE);
        if (order == RegistrationOrder.FIRST) {
            return FunctionCatalog.builder()
                    .register(dateTarget)
                    .register(exact)
                    .register(numberTarget)
                    .build();
        }
        return FunctionCatalog.builder()
                .register(numberTarget)
                .register(dateTarget)
                .register(exact)
                .build();
    }

    @Test
    @DisplayName("environment build rejects invalid typed defaults with stable construction errors")
    void environmentBuildRejectsInvalidTypedDefaultsWithStableConstructionErrors() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "not-a-number")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount")
                .hasMessageNotContaining("null");
    }

    @Test
    @DisplayName("environment build rejects duplicate custom function signatures with stable construction errors")
    void environmentBuildRejectsDuplicateCustomFunctionSignaturesWithStableConstructionErrors() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .function(pureFoldableDescriptor("same", ScalarType.NUMBER, ScalarType.NUMBER))
                .function(pureFoldableDescriptor("same", ScalarType.STRING, ScalarType.NUMBER))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same")
                .hasMessageContaining("already registered")
                .hasMessageNotContaining("null");
    }

    private static ExpressionEnvironment.Builder baseBuilder() {
        return ExpressionEnvironment.builder()
                .numericMode(NumericMode.FAST)
                .zoneId(ZoneId.of("Europe/Paris"))
                .mathContext(new MathContext(19, RoundingMode.FLOOR))
                .transcendentalMathContext(new MathContext(23, RoundingMode.CEILING))
                .strictMode(true)
                .maxCurrentItemDepth(7)
                .materializationLimit(1_234)
                .conversionProfileId("acceptance-v1");
    }

    private static ExpressionEnvironment identityDimensionEnvironment(IdentityDimension dimension) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .numericMode(dimension == IdentityDimension.NUMERIC_MODE ? NumericMode.FAST : NumericMode.DECIMAL)
                .zoneId(ZoneId.of(dimension == IdentityDimension.TIME_ZONE ? "America/Sao_Paulo" : "UTC"))
                .mathContext(dimension == IdentityDimension.MATH_CONTEXT_PRECISION
                        ? new MathContext(18, RoundingMode.HALF_UP)
                        : new MathContext(17, dimension == IdentityDimension.MATH_CONTEXT_ROUNDING
                                ? RoundingMode.DOWN
                                : RoundingMode.HALF_UP))
                .transcendentalMathContext(dimension == IdentityDimension.TRANSCENDENTAL_CONTEXT_PRECISION
                        ? new MathContext(22, RoundingMode.HALF_EVEN)
                        : new MathContext(21, dimension == IdentityDimension.TRANSCENDENTAL_CONTEXT_ROUNDING
                                ? RoundingMode.CEILING
                                : RoundingMode.HALF_EVEN))
                .strictMode(dimension == IdentityDimension.STRICT_MODE)
                .maxCurrentItemDepth(dimension == IdentityDimension.CURRENT_ITEM_DEPTH ? 6 : 5)
                .materializationLimit(dimension == IdentityDimension.MATERIALIZATION_LIMIT ? 1_001 : 1_000)
                .conversionProfileId(dimension == IdentityDimension.CONVERSION_PROFILE ? "profile-B" : "profile-A")
                .externalSymbol(
                        dimension == IdentityDimension.SYMBOL_NAME ? "total" : "amount",
                        dimension == IdentityDimension.SYMBOL_TYPE ? ScalarType.STRING : ScalarType.NUMBER)
                .externalSymbolWithDefault(
                        "businessDate",
                        ScalarType.DATE,
                        dimension == IdentityDimension.SYMBOL_DEFAULT
                                ? LocalDate.of(2026, 7, 7)
                                : LocalDate.of(2026, 7, 6))
                .function(dimension == IdentityDimension.FUNCTION_SIGNATURE
                        ? pureFoldableDescriptor("score", ScalarType.NUMBER, ScalarType.STRING)
                        : scoreFunction(dimension))
                .registerJavaType(dimension == IdentityDimension.JAVA_TYPE_OBJECT
                        ? JavaTypeCatalog.registerJavaType(JavaTypeCatalogTest.CustomerBean.class, "PreferredCustomer").build()
                        : customerJavaType(dimension));
        if (dimension == IdentityDimension.JAVA_TYPE_EXPOSURE) {
            return builder.registerJavaType(JavaTypeCatalog.registerJavaType(JavaTypeCatalogTest.AccountBean.class, "Account")
                    .withoutProperties()
                    .build()).build();
        }
        return builder.registerJavaType(JavaTypeCatalog.registerJavaType(JavaTypeCatalogTest.AccountBean.class, "Account").build())
                .build();
    }

    private static JavaTypeCatalog.RegisteredJavaType customerJavaType(IdentityDimension dimension) {
        if (dimension == IdentityDimension.JAVA_TYPE_CLASS) {
            return JavaTypeCatalog.registerJavaType(JavaTypeCatalogTest.AccountBean.class, "Customer").build();
        }
        return JavaTypeCatalog.registerJavaType(JavaTypeCatalogTest.CustomerBean.class, "Customer").build();
    }

    private static FunctionDescriptor scoreFunction(IdentityDimension dimension) {
        if (dimension == IdentityDimension.FUNCTION_FLAGS) {
            return impureDescriptor("score", ScalarType.NUMBER, ScalarType.NUMBER);
        }
        if (dimension == IdentityDimension.FUNCTION_RETURN_TYPE) {
            return pureFoldableDescriptor("score", ScalarType.STRING, ScalarType.NUMBER);
        }
        if (dimension == IdentityDimension.FUNCTION_IMPLEMENTATION) {
            return pureFoldableDescriptor(
                    "score",
                    ScalarType.NUMBER,
                    "acceptance:alternateScore",
                    ScalarType.NUMBER);
        }
        return pureFoldableDescriptor("score", ScalarType.NUMBER, ScalarType.NUMBER);
    }

    private static FunctionDescriptor pureFoldableDescriptor(
            String languageName,
            ExpressionType returnType,
            ExpressionType... parameterTypes) {
        return pureFoldableDescriptor(languageName, returnType, "acceptance:" + languageName, parameterTypes);
    }

    private static FunctionDescriptor pureFoldableDescriptor(
            String languageName,
            ExpressionType returnType,
            String implementationDescription,
            ExpressionType... parameterTypes) {
        FunctionDescriptor.Builder builder = FunctionDescriptor.builder(languageName)
                .parameterTypes(List.of(parameterTypes))
                .returnType(returnType)
                .implementationHandle(argumentHandle(languageName, parameterTypes.length), implementationDescription)
                .pure()
                .foldable();
        return builder.build();
    }

    private static FunctionDescriptor impureDescriptor(
            String languageName,
            ExpressionType returnType,
            ExpressionType... parameterTypes) {
        return FunctionDescriptor.builder(languageName)
                .parameterTypes(List.of(parameterTypes))
                .returnType(returnType)
                .implementationHandle(argumentHandle(languageName, parameterTypes.length), "acceptance:" + languageName)
                .build();
    }

    private static MethodHandle argumentHandle(String languageName, int arity) {
        return MethodHandles.dropArguments(
                MethodHandles.constant(Object.class, languageName),
                0,
                Collections.nCopies(arity, Object.class));
    }

    private enum IdentityDimension {
        BASELINE,
        SYMBOL_NAME,
        SYMBOL_TYPE,
        SYMBOL_DEFAULT,
        FUNCTION_SIGNATURE,
        FUNCTION_RETURN_TYPE,
        FUNCTION_IMPLEMENTATION,
        FUNCTION_FLAGS,
        JAVA_TYPE_OBJECT,
        JAVA_TYPE_CLASS,
        JAVA_TYPE_EXPOSURE,
        NUMERIC_MODE,
        TIME_ZONE,
        MATH_CONTEXT_PRECISION,
        MATH_CONTEXT_ROUNDING,
        TRANSCENDENTAL_CONTEXT_PRECISION,
        TRANSCENDENTAL_CONTEXT_ROUNDING,
        STRICT_MODE,
        CURRENT_ITEM_DEPTH,
        MATERIALIZATION_LIMIT,
        CONVERSION_PROFILE
    }

    private enum RegistrationOrder {
        FIRST,
        SECOND
    }
}
