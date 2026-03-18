package com.runestone.expeval2.api;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval2.perf.CrossModuleExpressionBenchmarkSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static void assertEquivalentForLiteralSeed(Expression legacy, MathExpression mk2, int seedIndex) {
        BigDecimal seed = CrossModuleExpressionBenchmarkSupport.literalSeed(seedIndex);
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(legacy, seed);
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(mk2, seed);

        assertThat(legacy.<BigDecimal>evaluate()).isEqualByComparingTo(mk2.compute());
    }

    private static void assertEquivalentForFrame(Expression legacy, MathExpression mk2,
                                                 CrossModuleExpressionBenchmarkSupport.Frame frame) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(legacy, frame);
        CrossModuleExpressionBenchmarkSupport.applyFrame(mk2, frame);

        assertThat(legacy.<BigDecimal>evaluate()).isEqualByComparingTo(mk2.compute());
    }
}
