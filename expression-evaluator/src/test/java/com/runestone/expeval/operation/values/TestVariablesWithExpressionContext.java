package com.runestone.expeval.operation.values;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.Map;

public class TestVariablesWithExpressionContext {

    @Test
    public void testWithVariableProvider() {
        ExpressionContext context = new ExpressionContext(varName -> switch (varName) {
            case "a" -> 1;
            case "b" -> 2;
            case "c" -> 3;
            default -> null;
        }, null, null);

        Expression expression = new Expression("a + b + c", context);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("6");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2 + 3");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
    }

    @Test
    public void testWithInternalAndUserVariableProvider() {
        ExpressionContext context = new ExpressionContext(varName -> switch (varName) {
            case "a" -> 1;
            case "b" -> 2;
            default -> null;
        }, null, null);

        ExpressionContext userContext = new ExpressionContext(varName -> switch (varName) {
            case "b" -> 2;
            case "c" -> 3;
            default -> null;
        }, null, null);

        Expression expression = new Expression("a + b + c", context);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate(userContext)).isEqualByComparingTo("6");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2 + 3");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
    }

    @Test
    public void testWithDictionary() {
        ExpressionContext context = new ExpressionContext(null, Map.of(
                "a", 1,
                "b", 2,
                "c", 3
        ), null);

        Expression expression = new Expression("a + b + c", context);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("6");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2 + 3");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
    }

    @Test
    public void testWithInternalAndUserDictionary() {
        ExpressionContext context = new ExpressionContext(null, Map.of(
                "a", 1,
                "b", 2
        ), null);

        ExpressionContext userContext = new ExpressionContext(null, Map.of(
                "b", 2,
                "c", 3
        ), null);

        Expression expression = new Expression("a + b + c", context);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate(userContext)).isEqualByComparingTo("6");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2 + 3");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
    }

    @Test
    public void testWithFunction() {
        ExpressionContext context = new ExpressionContext();
        context.putFunction("a", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(1));
        context.putFunction("b", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(2));
        context.putFunction("c", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(3));

        Expression expression = new Expression("a() + b() + c()", context);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("6");
        Assertions.assertThat(expression.toString()).isEqualTo("a() + b() + c()");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
    }

    @Test
    public void testWithInternalAndUserFunction() {
        ExpressionContext context = new ExpressionContext();
        context.putFunction("a", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(1));
        context.putFunction("b", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(2));

        ExpressionContext userContext = new ExpressionContext();
        userContext.putFunction("b", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(2));
        userContext.putFunction("c", MethodType.methodType(BigDecimal.class), (args) -> BigDecimal.valueOf(3));

        Expression expression = new Expression("a() + b() + c()", context);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate(userContext)).isEqualByComparingTo("6");
        Assertions.assertThat(expression.toString()).isEqualTo("a() + b() + c()");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
    }

}
