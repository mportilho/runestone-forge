package com.runestone.expeval.operation.values;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestConstantValues {

    @Test
    public void testNumericConstant() {
        Expression expression = new Expression("1");
        VerifyExpressionsTools.checkWarmUpCache(expression, 2);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1");
        Assertions.assertThat(expression.toString()).isEqualTo("1");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 2);
    }

    @Test
    public void testEulerNumericConstant() {
        Expression expression = new Expression("E");
        VerifyExpressionsTools.checkWarmUpCache(expression, 2);
        Assertions.assertThat(expression.toString()).isEqualTo("E");
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("2.718281828459045");
        Assertions.assertThat(expression.toString()).isEqualTo("E");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 2);
    }

    @Test
    public void testPiNumericConstant() {
        Expression expression = new Expression("PI");
        VerifyExpressionsTools.checkWarmUpCache(expression, 2);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3.141592653589793");
        Assertions.assertThat(expression.toString()).isEqualTo("pi");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 2);
    }

    @Test
    public void testBooleanConstant() {
        Expression expression = new Expression("true");
        VerifyExpressionsTools.checkWarmUpCache(expression, 2);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 2);
    }

}
