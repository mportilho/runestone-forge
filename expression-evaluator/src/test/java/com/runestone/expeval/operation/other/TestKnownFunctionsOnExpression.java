package com.runestone.expeval.operation.other;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestKnownFunctionsOnExpression {

    @Test
    public void testMax1() {
        Expression expression = new Expression("max([1])");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1");
        Assertions.assertThat(expression.toString()).isEqualTo("max([1])");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testMax2() {
        Expression expression = new Expression("max([1, 2])");
        VerifyExpressionsTools.checkWarmUpCache(expression, 5);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("2");
        Assertions.assertThat(expression.toString()).isEqualTo("max([1, 2])");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
    }

    @Test
    public void testMax3() {
        Expression expression = new Expression("max([1, 2, 3])");
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("max([1, 2, 3])");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
    }

    @Test
    public void testMin1() {
        Expression expression = new Expression("min(1)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1");
        Assertions.assertThat(expression.toString()).isEqualTo("min(1)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testMin2() {
        Expression expression = new Expression("min(2, 3)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("2");
        Assertions.assertThat(expression.toString()).isEqualTo("min(2, 3)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testMin3() {
        Expression expression = new Expression("min(3, 4, 5)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 5);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("min(3, 4, 5)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
    }

    @Test
    public void testConcat() {
        Expression expression = new Expression("concat(['a', 'b', 'c']) = 'abc'");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        VerifyExpressionsTools.checkCache(expression, 8);
    }

    @Test
    public void testConcatVArgs() {
        Expression expression = new Expression("concat('a', 'b', 'c') = 'abc'");
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        VerifyExpressionsTools.checkCache(expression, 7);
    }

}
