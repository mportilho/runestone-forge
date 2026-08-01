package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves issue #98's decimal scalar rules through the public compile/compute pipeline: equality and
 * ordering ignore scale-only differences, {@code mod} is exact regardless of {@code mathContext}, and
 * arithmetic results keep their naturally produced scale instead of a global normalization.
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

    @Test
    void additionAndSubtractionApplyTheConfiguredMathContext() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .mathContext(new MathContext(3, RoundingMode.HALF_UP))
                .build();
        assertThat(ExpressionCompiler.compileOrThrow("1.111111 + 2.222222", environment).asMath().compute())
                .isEqualByComparingTo("3.33");
        assertThat(ExpressionCompiler.compileOrThrow("5.555555 - 2.222222", environment).asMath().compute())
                .isEqualByComparingTo("3.33");
    }

    @Test
    void moduloIsExactAndFollowsTheDividendSignRegardlessOfMathContext() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .mathContext(new MathContext(2, RoundingMode.HALF_UP))
                .build();
        assertThat(ExpressionCompiler.compileOrThrow("10.12345 mod 3", environment).asMath().compute())
                .isEqualByComparingTo("1.12345");
        assertThat(ExpressionCompiler.compileOrThrow("-10 mod 3", environment).asMath().compute())
                .isEqualByComparingTo("-1");
        assertThat(ExpressionCompiler.compileOrThrow("10 mod -3", environment).asMath().compute())
                .isEqualByComparingTo("1");
    }

    @Test
    void percentFactorialAndComparisonIgnoreATightMathContext() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .mathContext(new MathContext(1, RoundingMode.HALF_UP))
                .build();
        assertThat(ExpressionCompiler.compileOrThrow("123.456%", environment).asMath().compute())
                .isEqualByComparingTo("1.23456");
        assertThat(ExpressionCompiler.compileOrThrow("5!", environment).asMath().compute())
                .isEqualByComparingTo("120");
        assertThat(ExpressionCompiler.compileOrThrow("1.00001 > 1.00000", environment).asLogical().compute())
                .isTrue();
    }

    @Test
    void arithmeticResultsKeepTheirNaturallyProducedScaleWithoutGlobalNormalization() {
        BigDecimal result = (BigDecimal) compute("2.50 * 1");

        assertThat(result.scale()).isEqualTo(2);
        assertThat(result.toPlainString()).isEqualTo("2.50");
    }

    private static Object compute(String source) {
        return ExpressionCompiler.compileOrThrow(source, ExpressionEnvironment.standard()).asResult().compute();
    }
}
