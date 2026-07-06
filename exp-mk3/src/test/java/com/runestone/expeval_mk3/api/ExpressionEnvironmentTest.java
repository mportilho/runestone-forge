package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
