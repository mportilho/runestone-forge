/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
