package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    @DisplayName("all compilation-relevant settings contribute to the environment ID")
    void allCompilationRelevantSettingsContributeToEnvironmentId() {
        ExpressionEnvironment baseline = ExpressionEnvironment.standard();

        assertThat(ExpressionEnvironment.builder().zoneId(ZoneId.of("America/Sao_Paulo")).build().environmentId())
                .isNotEqualTo(baseline.environmentId());
        assertThat(ExpressionEnvironment.builder().zoneId(alternateZoneId()).build().environmentId())
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
        assertThat(NullType.INSTANCE).isNotEqualTo(UnknownType.INSTANCE);
    }

    @Test
    @DisplayName("external symbols support defaults, declared types, and unknown declarations")
    void externalSymbolsSupportDefaultsDeclaredTypesAndUnknownDeclarations() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbolWithDefault("threshold", new BigDecimal("12.50"))
                .externalSymbol("enabled", ScalarType.BOOLEAN)
                .externalSymbolWithDefault("inferred", UnknownType.INSTANCE, "text")
                .externalSymbol("lateBound")
                .build();

        assertThat(environment.externalSymbols().asMap())
                .containsOnlyKeys("enabled", "inferred", "lateBound", "threshold");
        assertThat(environment.externalSymbols().asMap().get("threshold"))
                .isEqualTo(ExternalSymbol.withDefault("threshold", ScalarType.NUMBER, new BigDecimal("12.50")));
        assertThat(environment.externalSymbols().asMap().get("enabled"))
                .isEqualTo(ExternalSymbol.declared("enabled", ScalarType.BOOLEAN));
        assertThat(environment.externalSymbols().asMap().get("inferred"))
                .isEqualTo(ExternalSymbol.withDefault("inferred", ScalarType.STRING, "text"));
        assertThat(environment.externalSymbols().asMap().get("lateBound"))
                .isEqualTo(ExternalSymbol.unknown("lateBound"));

        ExternalSymbolCatalog catalog = ExternalSymbolCatalog.builder()
                .externalSymbolWithDefault("catalogThreshold", BigDecimal.ONE)
                .externalSymbol("catalogEnabled", ScalarType.BOOLEAN)
                .externalSymbol("catalogLateBound")
                .build();

        assertThat(catalog.asMap())
                .containsOnlyKeys("catalogEnabled", "catalogLateBound", "catalogThreshold");
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
    @DisplayName("reserved current temporal names cannot be declared as external symbols")
    void reservedCurrentTemporalNamesCannotBeDeclaredAsExternalSymbols() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder().externalSymbol("currDate"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().externalSymbol("currTime"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().externalSymbol("currDateTime"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> ExternalSymbol.unknown("currDate"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> ExternalSymbolCatalog.builder().externalSymbol("currTime"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
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
}
