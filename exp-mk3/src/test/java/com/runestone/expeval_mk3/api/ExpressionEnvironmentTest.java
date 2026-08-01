package com.runestone.expeval_mk3.api;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import com.runestone.converters.DataConverter;
import com.runestone.converters.impl.stable.DefaultDataConversionService;
import com.runestone.expeval_mk3.support.EnvironmentConfigurations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ExpressionEnvironmentTest {

    @Test
    @DisplayName("standard environment exposes stable defaults through the public API")
    void standardEnvironmentExposesStableDefaultsThroughPublicApi() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(environment.zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(environment.mathContext()).isEqualTo(MathContext.DECIMAL128);
        assertThat(environment.transcendentalMathContext()).isEqualTo(MathContext.DECIMAL128);
        assertThat(environment.maxCurrentItemDepth()).isEqualTo(32);
        assertThat(environment.maxMaterializedSize()).isEqualTo(10_000);
        assertThat(environment.maxFactorialInput()).isEqualTo(1_000);
        assertThat(environment.conversionProfileIdentity())
                .isEqualTo(DefaultDataConversionService.standard().conversionProfileIdentity());
        assertThat(environment.conversionProfileHash())
                .isEqualTo(DefaultDataConversionService.standard().conversionProfileHash());
        assertThat(environment).isSameAs(ExpressionEnvironment.standard());
        assertThat(environment.environmentId()).isEqualTo(ExpressionEnvironment.standard().environmentId());
    }

    @Test
    @DisplayName("each built environment receives a canonical UUID string")
    void eachBuiltEnvironmentReceivesACanonicalUuidString() {
        ExpressionEnvironment first = ExpressionEnvironment.builder().build();
        ExpressionEnvironment second = ExpressionEnvironment.builder().build();
        String firstRead = first.environmentId();

        assertThat(first).isNotSameAs(second);
        assertThat(firstRead).isNotEqualTo(second.environmentId());
        assertThat(UUID.fromString(firstRead).toString()).isEqualTo(firstRead);
        assertThat(first.environmentId()).isEqualTo(firstRead);
    }

    @Test
    @DisplayName("repeated builds from one unchanged builder receive different IDs")
    void repeatedBuildsFromOneUnchangedBuilderReceiveDifferentIds() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();

        assertThat(builder.build().environmentId()).isNotEqualTo(builder.build().environmentId());
    }

    @Test
    @DisplayName("built environments are immutable snapshots of mutable builders")
    void builtEnvironmentsAreImmutableSnapshotsOfMutableBuilders() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        ExpressionEnvironment first = builder.build();

        ExpressionEnvironment second = builder.maxFactorialInput(42).build();

        assertThat(first.maxFactorialInput()).isEqualTo(1_000);
        assertThat(second.maxFactorialInput()).isEqualTo(42);
        assertThat(first.environmentId()).isNotEqualTo(second.environmentId());
    }

    @Test
    @DisplayName("type vocabulary exposes scalar and composite expression types")
    void typeVocabularyExposesScalarAndCompositeExpressionTypes() {
        CollectionType numberCollection = new CollectionType(ScalarType.NUMBER);
        MapType stringMap = new MapType(ScalarType.STRING);

        assertThat(ScalarType.values())
                .containsExactly(
                        ScalarType.NUMBER,
                        ScalarType.BOOLEAN,
                        ScalarType.STRING,
                        ScalarType.DATE,
                        ScalarType.TIME,
                        ScalarType.DATETIME);
        assertThat(numberCollection.elementType()).isEqualTo(ScalarType.NUMBER);
        assertThat(stringMap.valueType()).isEqualTo(ScalarType.STRING);
        assertThat(new ObjectType("Customer")).isEqualTo(new ObjectType("Customer"));
        assertThat(new ObjectType("Customer")).isNotEqualTo(new ObjectType("Order"));
    }

    @Test
    @DisplayName("external symbols support defaults and declared known types")
    void externalSymbolsSupportDefaultsAndDeclaredKnownTypes() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("threshold", new BigDecimal("12.50"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("enabled", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("inferred", "text", ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(environment.externalSymbols().asMap())
                .containsOnlyKeys("enabled", "inferred", "threshold");
        assertThat(environment.externalSymbols().asMap().get("threshold"))
                .isEqualTo(ExternalSymbol.withDefault(
                        "threshold",
                        ScalarType.NUMBER,
                        new BigDecimal("12.50"),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE));
        assertThat(environment.externalSymbols().asMap().get("enabled"))
                .isEqualTo(ExternalSymbol.withDefault(
                        "enabled",
                        ScalarType.BOOLEAN,
                        true,
                        ExternalSymbolOverwritePolicy.FIXED));
        assertThat(environment.externalSymbols().asMap().get("inferred"))
                .isEqualTo(ExternalSymbol.withDefault(
                        "inferred",
                        ScalarType.STRING,
                        "text",
                        ExternalSymbolOverwritePolicy.FIXED));
        assertThat(environment.externalSymbols().asMap().get("threshold").overwritePolicy())
                .isEqualTo(ExternalSymbolOverwritePolicy.OVERRIDABLE);

        ExternalSymbolCatalog catalog = ExternalSymbolCatalog.builder()
                .externalSymbol("catalogThreshold", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("catalogEnabled", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();

        assertThat(catalog.asMap())
                .containsOnlyKeys("catalogEnabled", "catalogThreshold");
    }

    @Test
    @DisplayName("external symbol defaults are validated when the environment is built")
    void externalSymbolDefaultsAreValidatedWhenEnvironmentIsBuilt() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER, "not-a-number", ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ExternalSymbol.withDefault(
                "amount",
                ScalarType.NUMBER,
                "not-a-number",
                ExternalSymbolOverwritePolicy.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");

        Map<Object, Object> nonTextKeyedMap = new HashMap<>();
        nonTextKeyedMap.put(7, "seven");

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("labels", nonTextKeyedMap, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("text-keyed");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("missing", null, ExternalSymbolOverwritePolicy.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .externalSymbol("typedMissing", ScalarType.STRING, null, ExternalSymbolOverwritePolicy.FIXED))
                .withMessage("defaultValue");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("empty", List.of(), ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot infer");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("mixed", List.of("one", BigDecimal.ONE), ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("heterogeneous");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("items", new CollectionType(ScalarType.STRING), listWithNull(), ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("items");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("labels", new MapType(ScalarType.STRING), mapWithNullKey(), ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("text-keyed");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("labels", new MapType(ScalarType.STRING), mapWithNullValue(), ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("labels");
    }

    @Test
    @DisplayName("typed external symbol defaults are coerced by the configured boundary profile")
    void typedExternalSymbolDefaultsAreCoercedByConfiguredBoundaryProfile() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER, "12.50", ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("businessDate", ScalarType.DATE, "2026-07-06", ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("scores", new CollectionType(ScalarType.NUMBER), List.of("1.5", 2),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(environment.externalSymbols().asMap().get("amount").defaultValue())
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(new BigDecimal("12.50"));
        assertThat(environment.externalSymbols().asMap().get("businessDate").defaultValue())
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(environment.externalSymbols().asMap().get("scores").defaultValue())
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(List.of(new BigDecimal("1.5"), new BigDecimal("2")));
    }

    @Test
    @DisplayName("custom conversion services derive boundary profile metadata from the service")
    void customConversionServicesDeriveBoundaryProfileMetadataFromTheService() {
        DataConversionService conversionService = EnvironmentConfigurations.prefixedNumberConversionService(
                "prefixed-number:v1",
                "prefixed-number-hash:v1");

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .boundaryCoercion(conversionService)
                .externalSymbol("amount", ScalarType.NUMBER, "points:7", ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(environment.conversionProfileIdentity()).isEqualTo("prefixed-number:v1");
        assertThat(environment.conversionProfileHash()).isEqualTo("prefixed-number-hash:v1");
        assertThat(environment.boundaryCoercion().profileHash()).isEqualTo("prefixed-number-hash:v1");
        assertThat(environment.externalSymbols().asMap().get("amount").defaultValue())
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(new BigDecimal("7"));
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .boundaryCoercion(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dataConversionService");
    }

    @Test
    @DisplayName("boundary coercion answers expression type conversion support without expression runtime")
    void boundaryCoercionAnswersExpressionTypeConversionSupportWithoutExpressionRuntime() {
        BoundaryCoercion coercion = BoundaryCoercion.standard();

        assertThat(coercion.profileIdentity())
                .isEqualTo(DefaultDataConversionService.standard().conversionProfileIdentity());
        assertThat(coercion.profileHash())
                .isEqualTo(DefaultDataConversionService.standard().conversionProfileHash());
        assertThat(coercion.canConvert(String.class, ScalarType.NUMBER)).isTrue();
        assertThat(coercion.canConvert(String[].class, new CollectionType(ScalarType.NUMBER))).isTrue();
        assertThat(coercion.canConvert(int[].class, new CollectionType(ScalarType.NUMBER))).isTrue();
        assertThat(coercion.canConvert(Map.class, new MapType(ScalarType.NUMBER))).isFalse();
        assertThat(coercion.canConvert("12.50", ScalarType.NUMBER)).isTrue();
        assertThat(coercion.canConvert("not-a-number", ScalarType.NUMBER)).isFalse();
        assertThat(coercion.canConvert("2026-07-06", ScalarType.DATE)).isTrue();
        assertThat(coercion.canConvert(Map.of("amount", "12.50"), new MapType(ScalarType.NUMBER))).isTrue();
        assertThat(coercion.canConvert(Map.of(7, "seven"), new MapType(ScalarType.STRING))).isFalse();
        assertThat(coercion.canConvert("customer", new ObjectType("Customer"))).isFalse();
        assertThat(coercion.convertFunctionBindingFallback("12.50", ScalarType.NUMBER))
                .isEqualTo(new BigDecimal("12.50"));
        assertThatNullPointerException()
                .isThrownBy(() -> coercion.convertFunctionBindingFallback("12.50", null))
                .withMessage("targetType");
    }

    @Test
    @DisplayName("conversion profile metadata remains available for audit")
    void conversionProfileMetadataRemainsAvailableForAudit() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService(
                        "same-audit-identity",
                        "hash-a"))
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService(
                        "same-audit-identity",
                        "hash-b"))
                .build();

        assertThat(first.conversionProfileIdentity()).isEqualTo("same-audit-identity");
        assertThat(second.conversionProfileIdentity()).isEqualTo("same-audit-identity");
        assertThat(first.conversionProfileHash()).isEqualTo("hash-a");
        assertThat(second.conversionProfileHash()).isEqualTo("hash-b");
    }

    @Test
    @DisplayName("equivalent foldable conversion services preserve their profile contract")
    void equivalentFoldableConversionServicesPreserveTheirProfileContract() {
        DataConverter<String, BigDecimal> stringToNumber = rule(
                String.class,
                BigDecimal.class,
                "test.string.bigdecimal",
                (source, context) -> new BigDecimal(source));
        DataConverter<Integer, String> integerToString = rule(
                Integer.class,
                String.class,
                "test.integer.string",
                (source, context) -> source.toString());
        DataConversionService firstService = DefaultDataConversionService.withConverters(
                ConversionContext.standard(),
                List.of(stringToNumber, integerToString));
        DataConversionService secondService = DefaultDataConversionService.withConverters(
                ConversionContext.standard(),
                List.of(integerToString, stringToNumber));

        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .boundaryCoercion(firstService)
                .externalSymbol("amount", ScalarType.NUMBER, "12.50", ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .boundaryCoercion(secondService)
                .externalSymbol("amount", ScalarType.NUMBER, "12.50", ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(firstService.conversionProfileIdentity()).isEqualTo(secondService.conversionProfileIdentity());
        assertThat(firstService.conversionProfileHash()).isEqualTo(secondService.conversionProfileHash());
        assertThat(first.conversionProfileHash()).isEqualTo(firstService.conversionProfileHash());
        assertThat(first.externalSymbols().find("amount").orElseThrow().defaultValue().value())
                .isEqualTo(new BigDecimal("12.50"));
    }

    @Test
    @DisplayName("conversion profile audit identity and hash remain separately exposed")
    void conversionProfileAuditIdentityAndHashRemainSeparatelyExposed() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService(
                        "audit-identity-a",
                        "same-profile-hash"))
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService(
                        "audit-identity-b",
                        "same-profile-hash"))
                .build();

        assertThat(first.conversionProfileIdentity()).isEqualTo("audit-identity-a");
        assertThat(second.conversionProfileIdentity()).isEqualTo("audit-identity-b");
        assertThat(first.conversionProfileHash()).isEqualTo("same-profile-hash");
        assertThat(second.conversionProfileHash()).isEqualTo("same-profile-hash");
    }

    @Test
    @DisplayName("object defaults do not require reflective serialization to build an environment")
    void objectDefaultsDoNotRequireReflectiveSerializationToBuildAnEnvironment() {
        ThrowingGetter defaultValue = new ThrowingGetter();

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "opaque",
                        new ObjectType(ThrowingGetter.class.getName()),
                        defaultValue,
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(environment.externalSymbols().find("opaque").orElseThrow().defaultValue().value())
                .isSameAs(defaultValue);
    }

    @Test
    @DisplayName("reserved current temporal names cannot be declared as external symbols")
    void reservedCurrentTemporalNamesCannotBeDeclaredAsExternalSymbols() {
        for (CurrentTemporalValue currentTemporalValue : CurrentTemporalValue.values()) {
            String simpleName = currentTemporalValue.simpleName();

            assertThatThrownBy(() -> ExpressionEnvironment.builder()
                    .externalSymbol(simpleName, ScalarType.STRING, "value", ExternalSymbolOverwritePolicy.FIXED))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");
            assertThatThrownBy(() -> ExternalSymbol.withDefault(
                    simpleName,
                    ScalarType.STRING,
                    "value",
                    ExternalSymbolOverwritePolicy.FIXED))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");
            assertThatThrownBy(() -> ExternalSymbolCatalog.builder()
                    .externalSymbol(simpleName, ScalarType.STRING, "value", ExternalSymbolOverwritePolicy.FIXED))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");
        }
    }

    @Test
    @DisplayName("current temporal value contracts expose reserved names and semantic types")
    void currentTemporalValueContractsExposeReservedNamesAndSemanticTypes() {
        assertThat(CurrentTemporalValue.findBySimpleName("currDate"))
                .contains(CurrentTemporalValue.DATE);
        assertThat(CurrentTemporalValue.findBySimpleName("currTime"))
                .contains(CurrentTemporalValue.TIME);
        assertThat(CurrentTemporalValue.findBySimpleName("currDateTime"))
                .contains(CurrentTemporalValue.DATETIME);
        assertThat(CurrentTemporalValue.DATE.expressionType()).isEqualTo(ScalarType.DATE);
        assertThat(CurrentTemporalValue.TIME.expressionType()).isEqualTo(ScalarType.TIME);
        assertThat(CurrentTemporalValue.DATETIME.expressionType()).isEqualTo(ScalarType.DATETIME);
    }

    @Test
    @DisplayName("environment normalizes offset date-time literals for resolver metadata")
    void environmentNormalizesOffsetDateTimeLiteralsForResolverMetadata() {
        OffsetDateTime originalLiteral = OffsetDateTime.parse("2026-07-06T23:30:00+02:00");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("America/Sao_Paulo"))
                .build();

        OffsetDateTimeLiteralNormalization normalization = environment.normalizeOffsetDateTimeLiteral(originalLiteral);

        assertThat(normalization.originalLiteral()).isEqualTo(originalLiteral);
        assertThat(normalization.environmentZoneId()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
        assertThat(normalization.normalizedLocalDateTime()).isEqualTo(LocalDateTime.of(2026, 7, 6, 18, 30));
        assertThat(normalization.expressionType()).isEqualTo(ScalarType.DATETIME);
        assertThatThrownBy(() -> new OffsetDateTimeLiteralNormalization(
                originalLiteral,
                ZoneId.of("America/Sao_Paulo"),
                LocalDateTime.MIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("normalizedLocalDateTime");
    }

    @Test
    @DisplayName("invalid builder values fail at the public boundary")
    void invalidBuilderValuesFailAtPublicBoundary() {
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder().zoneId(null))
                .withMessage("zoneId");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder().mathContext(null))
                .withMessage("mathContext");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder().transcendentalMathContext(null))
                .withMessage("transcendentalMathContext");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder().boundaryCoercion(null))
                .withMessage("dataConversionService");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService(null, "hash")))
                .withMessage("profileIdentity");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService(" ", "hash")))
                .withMessage("conversion profile identity must not be blank");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService("identity", null)))
                .withMessage("profileHash");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .boundaryCoercion(EnvironmentConfigurations.prefixedNumberConversionService("identity", " ")))
                .withMessage("conversion profile hash must not be blank");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().maxCurrentItemDepth(-1))
                .withMessage("maxCurrentItemDepth must not be negative");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().maxMaterializedSize(-1))
                .withMessage("maxMaterializedSize must not be negative");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().maxFactorialInput(-1))
                .withMessage("maxFactorialInput must not be negative");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().mathContext(MathContext.UNLIMITED))
                .withMessage("mathContext must have positive precision");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .mathContext(new MathContext(10, RoundingMode.UNNECESSARY)))
                .withMessage("mathContext must not use RoundingMode.UNNECESSARY");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().transcendentalMathContext(MathContext.UNLIMITED))
                .withMessage("transcendentalMathContext must have positive precision");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .transcendentalMathContext(new MathContext(10, RoundingMode.UNNECESSARY)))
                .withMessage("transcendentalMathContext must not use RoundingMode.UNNECESSARY");
        ExpressionEnvironment supportedContexts = ExpressionEnvironment.builder()
                .mathContext(new MathContext(5, RoundingMode.FLOOR))
                .transcendentalMathContext(new MathContext(7, RoundingMode.CEILING))
                .build();
        assertThat(supportedContexts.mathContext()).isEqualTo(new MathContext(5, RoundingMode.FLOOR));
        assertThat(supportedContexts.transcendentalMathContext()).isEqualTo(new MathContext(7, RoundingMode.CEILING));
        ExpressionEnvironment zeroLimits = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(0)
                .maxMaterializedSize(0)
                .maxFactorialInput(0)
                .build();
        assertThat(zeroLimits.maxCurrentItemDepth()).isZero();
        assertThat(zeroLimits.maxMaterializedSize()).isZero();
        assertThat(zeroLimits.maxFactorialInput()).isZero();
    }

    @Test
    @DisplayName("runtime overrides reject nulls and fixed external symbols")
    void runtimeOverridesRejectNullsAndFixedExternalSymbols() {
        ExternalSymbol amount = ExternalSymbol.withDefault(
                "amount",
                ScalarType.NUMBER,
                BigDecimal.ONE,
                ExternalSymbolOverwritePolicy.OVERRIDABLE);
        ExternalSymbol fixed = ExternalSymbol.withDefault(
                "fixed",
                ScalarType.NUMBER,
                BigDecimal.ONE,
                ExternalSymbolOverwritePolicy.FIXED);

        assertThat(amount.coerceOverride("2.50", BoundaryCoercion.standard()))
                .isEqualTo(new BigDecimal("2.50"));
        assertThatThrownBy(() -> amount.coerceOverride(null, BoundaryCoercion.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("external symbol 'amount' override must not be null");
        assertThatThrownBy(() -> fixed.coerceOverride(BigDecimal.TEN, BoundaryCoercion.standard()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("external symbol 'fixed' is not overridable");
    }

    private static List<Object> listWithNull() {
        List<Object> values = new java.util.ArrayList<>();
        values.add("a");
        values.add(null);
        return values;
    }

    private static Map<Object, Object> mapWithNullKey() {
        Map<Object, Object> values = new HashMap<>();
        values.put(null, "value");
        return values;
    }

    private static Map<Object, Object> mapWithNullValue() {
        Map<Object, Object> values = new HashMap<>();
        values.put("key", null);
        return values;
    }

    private static <S, T> DataConverter<S, T> rule(
            Class<S> sourceType,
            Class<T> targetType,
            String identity,
            BiFunction<S, ConversionContext, T> conversion) {
        return new DataConverter<>() {
            @Override
            public Class<S> sourceType() {
                return sourceType;
            }

            @Override
            public Class<T> targetType() {
                return targetType;
            }

            @Override
            public String ruleIdentity() {
                return identity;
            }

            @Override
            public T convert(S source, ConversionContext context) {
                return conversion.apply(source, context);
            }
        };
    }

    static final class ThrowingGetter {
        public String getValue() {
            throw new AssertionError("getter must not be invoked while building the environment");
        }
    }

}
