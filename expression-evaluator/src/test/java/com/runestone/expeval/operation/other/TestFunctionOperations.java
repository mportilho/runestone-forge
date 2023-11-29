package com.runestone.expeval.operation.other;

import com.runestone.expeval.exceptions.ExpressionEvaluatorException;
import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.operation.other.tool.FunctionProviderClass;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


public class TestFunctionOperations {

    @Test
    public void testNumberFunctionOperationsWithoutCache() {
        Expression expression = new Expression("$.extractedNumber() + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("$.extractedNumber() + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 1);
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
    }

    @Test
    void testNumberFunctionOperationsWithCache() {
        Expression expression = new Expression("extractedNumber() + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("extractedNumber() + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    void testStringFunctionOperations() {
        Expression expression = new Expression("extractedString() = 'food'");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("extractedString() = 'food'");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    void testDateFunctionOperations() {
        Expression expression = new Expression("$.extractedDate() = currDate");
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("$.extractedDate() = currDate");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 0);
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);
    }

    @Test
    void testTimeFunctionOperations() {
        Expression expression = new Expression("extractedTime() = 02:03:00");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("extractedTime() = 02:03");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    void testDateTimeFunctionOperations() {
        Expression expression = new Expression("$.extractedDateTime() = (currDateTime setHours 2 setMinutes 3 setSeconds 0)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("$.extractedDateTime() = (currDateTime setHours 2 setMinutes 3 setSeconds 0)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
    }

    @Test
    void testBooleanFunctionOperations() {
        Expression expression = new Expression("extractedBoolean()");
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("extractedBoolean()");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 2);
        VerifyExpressionsTools.checkWarmUpCache(expression, 2);
    }

    @Test
    void testFunctionOperationsWithMultipleParameters() {
        Expression expression = new Expression("add(3, 4 + 2 - $.extractedNumber())");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        expression.addFunctionFromObject(new FunctionProviderClass());
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("8");
        Assertions.assertThat(expression.toString()).isEqualTo("add(3, 4 + 2 - $.extractedNumber())");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testThrowOnNonExistingFunction() {
        Expression expression = new Expression("notExistingFunction(3)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThatThrownBy(expression::evaluate)
                .isExactlyInstanceOf(ExpressionEvaluatorException.class)
                .hasMessageContaining("Function [notExistingFunction] with [1] parameter(s) not found");
    }

}
