package com.runestone.expeval2.api;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval2.perf.CrossModuleExpressionBenchmarkSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class CrossModuleExpressionEngineTest {

    @Test
    void shouldProduceEquivalentResultsForLiteralDenseScenario() {
        Expression legacy = CrossModuleExpressionBenchmarkSupport.newLegacyLiteralDenseExpression();
        MathExpression mk2 = CrossModuleExpressionBenchmarkSupport.newMk2LiteralDenseExpression();

        assertEquivalentForLiteralSeed(legacy, mk2, 0);
        assertEquivalentForLiteralSeed(legacy, mk2, 7);
        assertEquivalentForLiteralSeed(legacy, mk2, 63);
    }

    @Test
    void shouldProduceEquivalentResultsForVariableChurnScenario() {
        Expression legacy = CrossModuleExpressionBenchmarkSupport.newLegacyVariableChurnExpression();
        MathExpression mk2 = CrossModuleExpressionBenchmarkSupport.newMk2VariableChurnExpression();

        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(0));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(19));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(127));
    }

    @Test
    void shouldProduceEquivalentResultsForUserFunctionScenario() {
        Expression legacy = CrossModuleExpressionBenchmarkSupport.newLegacyUserFunctionExpression();
        MathExpression mk2 = CrossModuleExpressionBenchmarkSupport.newMk2UserFunctionExpression();

        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.userFunctionFrame(0));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.userFunctionFrame(33));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.userFunctionFrame(191));
    }

    @Test
    void shouldProduceEquivalentResultsForConditionalScenario() {
        Expression legacy = CrossModuleExpressionBenchmarkSupport.newLegacyConditionalExpression();
        MathExpression mk2 = CrossModuleExpressionBenchmarkSupport.newMk2ConditionalExpression();

        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(0));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(41));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(200));
    }

    @Test
    void shouldProduceEquivalentResultsForLogarithmChainScenario() {
        Expression legacy = CrossModuleExpressionBenchmarkSupport.newLegacyLogarithmChainExpression();
        MathExpression mk2 = CrossModuleExpressionBenchmarkSupport.newMk2LogarithmChainExpression();

        // The two modules use different MathContext precisions for transcendental functions,
        // so results are numerically equivalent but may differ beyond ~15 significant digits.
        assertApproximatelyEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(0));
        assertApproximatelyEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(41));
        assertApproximatelyEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(200));
    }

    @Test
    void shouldProduceEquivalentResultsForPowerChainScenario() {
        Expression legacy = CrossModuleExpressionBenchmarkSupport.newLegacyPowerChainExpression();
        MathExpression mk2 = CrossModuleExpressionBenchmarkSupport.newMk2PowerChainExpression();

        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(0));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(41));
        assertEquivalentForFrame(legacy, mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(200));
    }

    private static void assertEquivalentForLiteralSeed(Expression legacy, MathExpression mk2, int seedIndex) {
        BigDecimal seed = CrossModuleExpressionBenchmarkSupport.literalSeed(seedIndex);
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(legacy, seed);

        assertThat(legacy.<BigDecimal>evaluate()).isEqualByComparingTo(
                mk2.compute(CrossModuleExpressionBenchmarkSupport.literalSeedToMap(seed)));
    }

    private static void assertEquivalentForFrame(Expression legacy, MathExpression mk2,
                                                 CrossModuleExpressionBenchmarkSupport.Frame frame) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(legacy, frame);

        assertThat(legacy.<BigDecimal>evaluate()).isEqualByComparingTo(
                mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(frame)));
    }

    private static void assertApproximatelyEquivalentForFrame(Expression legacy, MathExpression mk2,
                                                              CrossModuleExpressionBenchmarkSupport.Frame frame) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(legacy, frame);

        assertThat(legacy.<BigDecimal>evaluate().doubleValue())
            .isCloseTo(mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(frame)).doubleValue(), offset(1e-9));
    }
}
