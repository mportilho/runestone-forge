package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves issue #98's decimal scalar rules through the public compile/compute pipeline: equality and
 * ordering ignore scale-only differences, {@code mod} is exact regardless of {@code mathContext}, and
 * arithmetic results keep their naturally produced scale instead of a global normalization. Also proves
 * ADR 0021: {@code +}/{@code -} are exact regardless of {@code mathContext}, while {@code *} always rounds.
 */
class DecimalScalarSemanticsRuntimeTest {

    @Test
    void equalityIgnoresScaleOnlyDifferences() {
        assertThat((Boolean) compute("1 = 1.0")).isTrue();
        assertThat((Boolean) compute("1 = 1.00")).isTrue();
        assertThat((Boolean) compute("1.0 = 1.00")).isTrue();
    }

    @Test
    void orderingIgnoresScaleOnlyDifferences() {
        assertThat((Boolean) compute("1.00 >= 1")).isTrue();
        assertThat((Boolean) compute("1 <= 1.00")).isTrue();
        assertThat((Boolean) compute("1.00 > 1")).isFalse();
    }

    private static final ExpressionEnvironment NARROW_CONTEXT_ENVIRONMENT = ExpressionEnvironment.builder()
            .mathContext(new MathContext(3, RoundingMode.HALF_UP))
            .build();

    @Test
    void additionAndSubtractionAreExactRegardlessOfMathContext() {
        // ADR 0021: + and - have a bounded, non-compounding result scale (max(scale1, scale2));
        // rounding them would only discard exact digits, so a narrow mathContext must not truncate them.
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("1.111111 + 2.222222", NARROW_CONTEXT_ENVIRONMENT).asMath().compute())
                .isEqualByComparingTo("3.333333");
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("5.555555 - 2.222222", NARROW_CONTEXT_ENVIRONMENT).asMath().compute())
                .isEqualByComparingTo("3.333333");
    }

    @Test
    void multiplyAppliesTheConfiguredMathContextEvenForASingleOperation() {
        // ADR 0021: * has a compounding result scale (scale1 + scale2); it always rounds, unlike +/-,
        // because a scale-dependent rounding rule would not be a statable contract.
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("1.111111 * 2.222222", NARROW_CONTEXT_ENVIRONMENT).asMath().compute())
                .isEqualByComparingTo("2.47");
    }

    @Test
    void moduloIsExactAndFollowsTheDividendSignRegardlessOfMathContext() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .mathContext(new MathContext(2, RoundingMode.HALF_UP))
                .build();
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("10.12345 mod 3", environment).asMath().compute())
                .isEqualByComparingTo("1.12345");
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("-10 mod 3", environment).asMath().compute())
                .isEqualByComparingTo("-1");
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("10 mod -3", environment).asMath().compute())
                .isEqualByComparingTo("1");
    }

    @Test
    void percentFactorialAndComparisonIgnoreATightMathContext() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .mathContext(new MathContext(1, RoundingMode.HALF_UP))
                .build();
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("123.456%", environment).asMath().compute())
                .isEqualByComparingTo("1.23456");
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("5!", environment).asMath().compute())
                .isEqualByComparingTo("120");
        assertThat(ExpressionEngine.defaultEngine().compileOrThrow("1.00001 > 1.00000", environment).asLogical().compute())
                .isTrue();
    }

    @Test
    void arithmeticResultsKeepTheirNaturallyProducedScaleWithoutGlobalNormalization() {
        BigDecimal result = (BigDecimal) compute("2.50 * 1");

        assertThat(result.scale()).isEqualTo(2);
        assertThat(result.toPlainString()).isEqualTo("2.50");
    }

    private static Object compute(String source) {
        return ExpressionEngine.defaultEngine().compileOrThrow(source, ExpressionEnvironment.standard()).asResult().compute();
    }
}
