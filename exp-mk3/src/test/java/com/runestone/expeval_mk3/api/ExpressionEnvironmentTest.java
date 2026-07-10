package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
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
