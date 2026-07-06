package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEnvironmentTest {

    @Test
    @DisplayName("standard environment is the canonical default environment")
    void standardEnvironmentIsCanonicalDefaultEnvironment() {
        ExpressionEnvironment standard = ExpressionEnvironment.standard();
        ExpressionEnvironment defaultBuilder = ExpressionEnvironment.builder().build();

        assertThat(standard.environmentId()).isEqualTo(defaultBuilder.environmentId());
        assertThat(standard.numericMode()).isEqualTo(NumericMode.DECIMAL);
    }

    @Test
    @DisplayName("equivalent builder content produces the same environment id")
    void equivalentBuilderContentProducesSameEnvironmentId() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .numericMode(NumericMode.FAST)
                .zoneId(ZoneId.of("Europe/Paris"))
                .mathContext(new MathContext(19, RoundingMode.FLOOR))
                .transcendentalMathContext(new MathContext(23, RoundingMode.CEILING))
                .strictMode(true)
                .maxCurrentItemDepth(7)
                .materializationLimit(1_234)
                .conversionProfileId("profile-A")
                .build();

        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .conversionProfileId("profile-A")
                .materializationLimit(1_234)
                .maxCurrentItemDepth(7)
                .strictMode(true)
                .transcendentalMathContext(new MathContext(23, RoundingMode.CEILING))
                .mathContext(new MathContext(19, RoundingMode.FLOOR))
                .zoneId(ZoneId.of("Europe/Paris"))
                .numericMode(NumericMode.FAST)
                .build();

        assertThat(first.environmentId()).isEqualTo(second.environmentId());
    }

    @Test
    @DisplayName("each compilation relevant environment field changes the environment id")
    void eachCompilationRelevantEnvironmentFieldChangesEnvironmentId() {
        ExpressionEnvironment baseline = customEnvironmentBuilder().build();

        List<ExpressionEnvironmentId> changedIds = List.of(
                customEnvironmentBuilder().zoneId(ZoneId.of("Asia/Tokyo")).build().environmentId(),
                customEnvironmentBuilder().numericMode(NumericMode.DECIMAL).build().environmentId(),
                customEnvironmentBuilder().mathContext(new MathContext(20, RoundingMode.FLOOR)).build().environmentId(),
                customEnvironmentBuilder().mathContext(new MathContext(19, RoundingMode.CEILING)).build().environmentId(),
                customEnvironmentBuilder()
                        .transcendentalMathContext(new MathContext(24, RoundingMode.CEILING))
                        .build()
                        .environmentId(),
                customEnvironmentBuilder()
                        .transcendentalMathContext(new MathContext(23, RoundingMode.FLOOR))
                        .build()
                        .environmentId(),
                customEnvironmentBuilder().strictMode(false).build().environmentId(),
                customEnvironmentBuilder().maxCurrentItemDepth(8).build().environmentId(),
                customEnvironmentBuilder().materializationLimit(1_235).build().environmentId(),
                customEnvironmentBuilder().conversionProfileId("profile-B").build().environmentId());

        assertThat(changedIds).doesNotContain(baseline.environmentId());
        assertThat(changedIds).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("built environments are immutable snapshots")
    void builtEnvironmentsAreImmutableSnapshots() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("UTC"));
        ExpressionEnvironment initial = builder.build();

        ExpressionEnvironment changed = builder.zoneId(ZoneId.of("America/Sao_Paulo")).build();
        ExpressionEnvironment derived = initial.toBuilder().strictMode(!initial.strictMode()).build();

        assertThat(initial.zoneId()).isEqualTo(ZoneId.of("UTC"));
        assertThat(changed.zoneId()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
        assertThat(derived.strictMode()).isNotEqualTo(initial.strictMode());
        assertThat(changed.environmentId()).isNotEqualTo(initial.environmentId());
        assertThat(derived.environmentId()).isNotEqualTo(initial.environmentId());
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
    @DisplayName("external symbols contribute deterministically to the environment id")
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

    private static ExpressionEnvironment.Builder customEnvironmentBuilder() {
        return ExpressionEnvironment.builder()
                .numericMode(NumericMode.FAST)
                .zoneId(ZoneId.of("Europe/Paris"))
                .mathContext(new MathContext(19, RoundingMode.FLOOR))
                .transcendentalMathContext(new MathContext(23, RoundingMode.CEILING))
                .strictMode(true)
                .maxCurrentItemDepth(7)
                .materializationLimit(1_234)
                .conversionProfileId("profile-A");
    }
}
