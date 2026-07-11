package com.runestone.expeval_mk3.api;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ExpressionEnvironmentTest {

    @Test
    @DisplayName("standard environment exposes stable defaults through the public API")
    void standardEnvironmentExposesStableDefaultsThroughPublicApi() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(environment.numericMode()).isEqualTo(NumericMode.DECIMAL);
        assertThat(environment.zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(environment.mathContext()).isEqualTo(MathContext.DECIMAL128);
        assertThat(environment.transcendentalMathContext()).isEqualTo(MathContext.DECIMAL128);
        assertThat(environment.strictMode()).isFalse();
        assertThat(environment.maxCurrentItemDepth()).isEqualTo(32);
        assertThat(environment.materializationLimit()).isEqualTo(10_000);
        assertThat(environment.conversionProfileIdentity()).isEqualTo("standard");
        assertThat(environment.environmentId()).isEqualTo(ExpressionEnvironment.standard().environmentId());
    }

    @Test
    @DisplayName("equivalent builder content produces the same canonical environment ID")
    void equivalentBuilderContentProducesSameCanonicalEnvironmentId() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("America/Sao_Paulo"))
                .numericMode(NumericMode.FAST)
                .mathContext(new MathContext(20, RoundingMode.HALF_UP))
                .transcendentalMathContext(new MathContext(34, RoundingMode.HALF_EVEN))
                .strictMode(true)
                .maxCurrentItemDepth(7)
                .materializationLimit(512)
                .conversionProfileIdentity("tenant-a:v2")
                .build();

        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .conversionProfileIdentity("tenant-a:v2")
                .materializationLimit(512)
                .maxCurrentItemDepth(7)
                .strictMode(true)
                .transcendentalMathContext(new MathContext(34, RoundingMode.HALF_EVEN))
                .mathContext(new MathContext(20, RoundingMode.HALF_UP))
                .numericMode(NumericMode.FAST)
                .zoneId(ZoneId.of("America/Sao_Paulo"))
                .build();

        assertThat(first).isNotSameAs(second);
        assertThat(first.environmentId()).isEqualTo(second.environmentId());
        assertThat(first.environmentId().value()).startsWith("sha256:");
    }

    @Test
    @DisplayName("otherwise equal environments with different time zones have different environment IDs")
    void otherwiseEqualEnvironmentsWithDifferentTimeZonesHaveDifferentEnvironmentIds() {
        ExpressionEnvironment utc = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("UTC"))
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();
        ExpressionEnvironment saoPaulo = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("America/Sao_Paulo"))
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        assertThat(utc.environmentId()).isNotEqualTo(saoPaulo.environmentId());
    }

    @Test
    @DisplayName("all compilation-relevant settings contribute to the environment ID")
    void allCompilationRelevantSettingsContributeToEnvironmentId() {
        ExpressionEnvironment baseline = ExpressionEnvironment.standard();
        ZoneId changedZone = alternateZoneId();

        assertThat(changedZone).isNotEqualTo(ZoneId.systemDefault());
        assertThat(ExpressionEnvironment.builder().zoneId(changedZone).build().environmentId())
                .isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().numericMode(NumericMode.FAST).build().environmentId())
                .isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().mathContext(new MathContext(16, RoundingMode.HALF_EVEN)).build()
                .environmentId()).isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().transcendentalMathContext(new MathContext(16, RoundingMode.HALF_EVEN))
                .build().environmentId()).isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().strictMode(true).build().environmentId())
                .isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().maxCurrentItemDepth(33).build().environmentId())
                .isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().materializationLimit(10_001).build().environmentId())
                .isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().conversionProfileIdentity("custom").build().environmentId())
                .isNotEqualTo(baseline.environmentId());
    }

    @Test
    @DisplayName("built environments are immutable snapshots of mutable builders")
    void builtEnvironmentsAreImmutableSnapshotsOfMutableBuilders() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        ExpressionEnvironment first = builder.build();

        ExpressionEnvironment second = builder.numericMode(NumericMode.FAST).build();

        assertThat(first.numericMode()).isEqualTo(NumericMode.DECIMAL);
        assertThat(second.numericMode()).isEqualTo(NumericMode.FAST);
        assertThat(first.environmentId()).isNotEqualTo(second.environmentId());
    }

    @Test
    @DisplayName("type vocabulary exposes scalar and composite expression types")
    void typeVocabularyExposesScalarAndCompositeExpressionTypes() {
        VectorType numberVector = new VectorType(ScalarType.NUMBER);
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
        assertThat(numberVector.elementType()).isEqualTo(ScalarType.NUMBER);
        assertThat(numberCollection.elementType()).isEqualTo(ScalarType.NUMBER);
        assertThat(numberVector).isNotEqualTo(numberCollection);
        assertThat(stringMap.valueType()).isEqualTo(ScalarType.STRING);
        assertThat(new ObjectType("Customer")).isEqualTo(new ObjectType("Customer"));
        assertThat(new ObjectType("Customer")).isNotEqualTo(new ObjectType("Order"));
    }

    @Test
    @DisplayName("external symbols support defaults and declared known types")
    void externalSymbolsSupportDefaultsAndDeclaredKnownTypes() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("threshold", new BigDecimal("12.50"))
                .externalSymbol("enabled", ScalarType.BOOLEAN)
                .externalSymbolWithDefault("inferred", "text")
                .build();

        assertThat(environment.externalSymbols().asMap())
                .containsOnlyKeys("enabled", "inferred", "threshold");
        assertThat(environment.externalSymbols().asMap().get("threshold"))
                .isEqualTo(ExternalSymbol.withDefault("threshold", ScalarType.NUMBER, new BigDecimal("12.50")));
        assertThat(environment.externalSymbols().asMap().get("enabled"))
                .isEqualTo(ExternalSymbol.declared("enabled", ScalarType.BOOLEAN));
        assertThat(environment.externalSymbols().asMap().get("inferred"))
                .isEqualTo(ExternalSymbol.withDefault("inferred", ScalarType.STRING, "text"));

        ExternalSymbolCatalog catalog = ExternalSymbolCatalog.builder()
                .externalSymbolWithDefault("catalogThreshold", BigDecimal.ONE)
                .externalSymbol("catalogEnabled", ScalarType.BOOLEAN)
                .build();

        assertThat(catalog.asMap())
                .containsOnlyKeys("catalogEnabled", "catalogThreshold");
    }

    @Test
    @DisplayName("external symbol defaults are validated when the environment is built")
    void externalSymbolDefaultsAreValidatedWhenEnvironmentIsBuilt() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "not-a-number")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ExternalSymbol.withDefault("amount", ScalarType.NUMBER, "not-a-number"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");

        Map<Object, Object> nonTextKeyedMap = new HashMap<>();
        nonTextKeyedMap.put(7, "seven");

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("labels", nonTextKeyedMap)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("text-keyed");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("missing", null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("empty", List.of())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot infer");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbolWithDefault("mixed", List.of("one", BigDecimal.ONE))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("heterogeneous");
    }

    @Test
    @DisplayName("typed external symbol defaults are coerced by the configured boundary profile")
    void typedExternalSymbolDefaultsAreCoercedByConfiguredBoundaryProfile() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "12.50")
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, "2026-07-06")
                .externalSymbolWithDefault("scores", new VectorType(ScalarType.NUMBER), List.of("1.5", 2))
                .build();

        assertThat(environment.externalSymbols().asMap().get("amount").defaultValue())
                .get()
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(new BigDecimal("12.50"));
        assertThat(environment.externalSymbols().asMap().get("businessDate").defaultValue())
                .get()
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(environment.externalSymbols().asMap().get("scores").defaultValue())
                .get()
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(List.of(new BigDecimal("1.5"), new BigDecimal("2")));
    }

    @Test
    @DisplayName("custom conversion services are identified by an explicit stable profile")
    void customConversionServicesAreIdentifiedByExplicitStableProfile() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .boundaryCoercion("prefixed-number:v1", new PrefixedNumberConversionService())
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "points:7")
                .build();

        assertThat(environment.conversionProfileIdentity()).isEqualTo("prefixed-number:v1");
        assertThat(environment.externalSymbols().asMap().get("amount").defaultValue())
                .get()
                .extracting(ExternalSymbolDefault::value)
                .isEqualTo(new BigDecimal("7"));
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .boundaryCoercion(" ", new PrefixedNumberConversionService()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profile");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .boundaryCoercion("prefixed-number:v1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dataConversionService");
    }

    @Test
    @DisplayName("boundary coercion answers expression type conversion support without expression runtime")
    void boundaryCoercionAnswersExpressionTypeConversionSupportWithoutExpressionRuntime() {
        BoundaryCoercion coercion = BoundaryCoercion.standard();

        assertThat(coercion.profileIdentity()).isEqualTo("standard");
        assertThat(coercion.canConvert(String.class, ScalarType.NUMBER)).isTrue();
        assertThat(coercion.canConvert(String[].class, new VectorType(ScalarType.NUMBER))).isTrue();
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
    @DisplayName("external symbols contribute deterministically to the environment ID")
    void externalSymbolsContributeDeterministicallyToEnvironmentId() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 6))
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER)
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 6))
                .build();
        ExpressionEnvironment differentDefault = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("businessDate", ScalarType.DATE, LocalDate.of(2026, 7, 7))
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        Map<String, Object> firstMap = new LinkedHashMap<>();
        firstMap.put("b", BigDecimal.valueOf(2));
        firstMap.put("a", BigDecimal.ONE);
        Map<String, Object> sameMapDifferentOrder = new LinkedHashMap<>();
        sameMapDifferentOrder.put("a", BigDecimal.ONE);
        sameMapDifferentOrder.put("b", BigDecimal.valueOf(2));

        ExpressionEnvironment firstMapDefault = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("labels", new MapType(ScalarType.NUMBER), firstMap)
                .build();
        ExpressionEnvironment sameMapDefaultDifferentOrder = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("labels", new MapType(ScalarType.NUMBER), sameMapDifferentOrder)
                .build();

        assertThat(first.environmentId()).isEqualTo(sameContentDifferentOrder.environmentId());
        assertThat(first.environmentId()).isNotEqualTo(differentDefault.environmentId());
        assertThat(firstMapDefault.environmentId()).isEqualTo(sameMapDefaultDifferentOrder.environmentId());
    }

    @Test
    @DisplayName("conversion profile differences affect the environment ID")
    void conversionProfileDifferencesAffectEnvironmentId() {
        ExpressionEnvironment standardProfile = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "12.50")
                .build();
        ExpressionEnvironment alternateProfile = ExpressionEnvironment.builder()
                .conversionProfileIdentity("standard-copy:v2")
                .externalSymbolWithDefault("amount", ScalarType.NUMBER, "12.50")
                .build();

        assertThat(standardProfile.environmentId()).isNotEqualTo(alternateProfile.environmentId());
    }

    @Test
    @DisplayName("reserved current temporal names cannot be declared as external symbols")
    void reservedCurrentTemporalNamesCannotBeDeclaredAsExternalSymbols() {
        for (CurrentTemporalValue currentTemporalValue : CurrentTemporalValue.values()) {
            String simpleName = currentTemporalValue.simpleName();

            assertThatThrownBy(() -> ExpressionEnvironment.builder().externalSymbol(simpleName, ScalarType.STRING))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");
            assertThatThrownBy(() -> ExternalSymbol.declared(simpleName, ScalarType.STRING))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reserved");
            assertThatThrownBy(() -> ExternalSymbolCatalog.builder().externalSymbol(simpleName, ScalarType.STRING))
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
                .isThrownBy(() -> ExpressionEnvironment.builder().numericMode(null))
                .withMessage("numericMode");
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
                .isThrownBy(() -> ExpressionEnvironment.builder().conversionProfileIdentity(null))
                .withMessage("conversionProfileIdentity");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().maxCurrentItemDepth(-1))
                .withMessage("maxCurrentItemDepth must not be negative");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().materializationLimit(-1))
                .withMessage("materializationLimit must not be negative");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExpressionEnvironment.builder().conversionProfileIdentity("  "))
                .withMessage("conversionProfileIdentity must not be blank");

        ExpressionEnvironment zeroLimits = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(0)
                .materializationLimit(0)
                .build();
        assertThat(zeroLimits.maxCurrentItemDepth()).isZero();
        assertThat(zeroLimits.materializationLimit()).isZero();
    }

    private static ZoneId alternateZoneId() {
        ZoneId systemDefault = ZoneId.systemDefault();
        ZoneId utc = ZoneOffset.UTC;
        if (!systemDefault.equals(utc)) {
            return utc;
        }
        return ZoneId.of("America/Sao_Paulo");
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
}
