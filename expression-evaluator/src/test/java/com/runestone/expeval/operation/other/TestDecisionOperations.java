package com.runestone.expeval.operation.other;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestDecisionOperations {

    // --------------------------------- Logical Decision Expression ---------------------------------

    @Test
    public void testNumberDecisionExpression() {
        Expression expression = new Expression("if !true then 1 else 0 endif");
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("0");
        Assertions.assertThat(expression.toString()).isEqualTo("if !true then 1 else 0 endif");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
    }

    @Test
    public void testLogicalDecisionExpression() {
        Expression expression = new Expression("if !true then true else false endif");
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("if !true then true else false endif");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
    }

    @Test
    public void testStringDecisionExpression() {
        Expression expression = new Expression("if !true then 'a' else 'b' endif = 'b'");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if !true then 'a' else 'b' endif = 'b'");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    @Test
    public void testDateDecisionExpression() {
        Expression expression = new Expression("if !true then 2000-01-01 else 2000-01-02 endif = 2000-01-02");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if !true then 2000-01-01 else 2000-01-02 endif = 2000-01-02");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    @Test
    public void testTimeDecisionExpression() {
        Expression expression = new Expression("if !true then 12:00:00 else 12:00:01 endif = 12:00:01");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if !true then 12:00:00 else 12:00:01 endif = 12:00:01");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    @Test
    public void testDateTimeDecisionExpression() {
        Expression expression = new Expression("if !true then 2000-01-01T12:00:00 else 2000-01-02T12:00:01 endif = 2000-01-02T12:00:01");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if !true then 2000-01-01T12:00:00 else 2000-01-02T12:00:01 endif = 2000-01-02T12:00:01");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    // --------------------------------- Logical Function Decision Expression ---------------------------------

    @Test
    public void testNumberFunctionDecisionExpression() {
        Expression expression = new Expression("if(!true, 1, 0)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("0");
        Assertions.assertThat(expression.toString()).isEqualTo("if(!true, 1, 0)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
    }


    @Test
    public void testLogicalFunctionDecisionExpression() {
        Expression expression = new Expression("if(!true, true, false)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("if(!true, true, false)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
    }

    @Test
    public void testStringFunctionDecisionExpression() {
        Expression expression = new Expression("if(!true, 'a', 'b') = 'b'");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if(!true, 'a', 'b') = 'b'");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    @Test
    public void testDateFunctionDecisionExpression() {
        Expression expression = new Expression("if(!true, 2000-01-01, 2000-01-02) = 2000-01-02");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if(!true, 2000-01-01, 2000-01-02) = 2000-01-02");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    @Test
    public void testTimeFunctionDecisionExpression() {
        Expression expression = new Expression("if(!true, 12:00:00, 12:00:01) = 12:00:01");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if(!true, 12:00:00, 12:00:01) = 12:00:01");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    @Test
    public void testDateTimeFunctionDecisionExpression() {
        Expression expression = new Expression("if(!true, 2000-01-01T12:00:00, 2000-01-02T12:00:01) = 2000-01-02T12:00:01");
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("if(!true, 2000-01-01T12:00:00, 2000-01-02T12:00:01) = 2000-01-02T12:00:01");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
    }

    // --------------------------------- Logical Decision Expression - Complex Expressions ---------------------------------

    @Test
    public void testDecisionOperations() {
        Expression exp1 = new Expression("if true then 7 elsif false then 1 else if true then 1 else 0 endif endif");
        VerifyExpressionsTools.checkWarmUpCache(exp1, 10);
        Assertions.assertThat(exp1.<BigDecimal>evaluate()).isEqualByComparingTo("7");
        Assertions.assertThat(exp1.toString()).isEqualTo("if true then 7 elsif false then 1 else if true then 1 else 0 endif endif");
        VerifyExpressionsTools.commonVerifications(exp1);
        VerifyExpressionsTools.checkCache(exp1, 4);

        Expression exp2 = new Expression("if false then 0 elsif false then 1 else if true then 7 else 0 endif endif");
        VerifyExpressionsTools.checkWarmUpCache(exp2, 10);
        Assertions.assertThat(exp2.<BigDecimal>evaluate()).isEqualByComparingTo("7");
        Assertions.assertThat(exp2.toString()).isEqualTo("if false then 0 elsif false then 1 else if true then 7 else 0 endif endif");
        VerifyExpressionsTools.commonVerifications(exp2);
        VerifyExpressionsTools.checkCache(exp2, 7);
    }

    @Test
    public void testDecisionOperationWithVariable() {
        Expression expression = new Expression("if(80% <= AIS and AIS <= 100%, 0, 60% <= AIS and AIS < 80%, 5%, 10%)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 13);
        Assertions.assertThat(expression.toString()).isEqualTo("if(80% <= AIS and AIS <= 100%, 0, 60% <= AIS and AIS < 80%, 5%, 10%)");
        expression.setVariable("AIS", new BigDecimal("0.9"));
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("0");
        Assertions.assertThat(expression.toString()).isEqualTo("if(80% <= 0.9 and 0.9 <= 100%, 0, 60% <= 0.9 and 0.9 < 80%, 5%, 10%)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 11);
        VerifyExpressionsTools.checkWarmUpCache(expression, 22);
    }

}
