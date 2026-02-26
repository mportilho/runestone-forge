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

package com.runestone.expeval.operation.math;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionOptions;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

public class TestMathOperations {

    private static final ExpressionOptions OPTIONS = ExpressionOptions.of(MathContext.DECIMAL64, 8);

    @Test
    public void testAddition() {
        Expression expression = new Expression("1 + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testDegree() {
        Expression expression = new Expression("1°", OPTIONS);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("0.01745329");
        Assertions.assertThat(expression.toString()).isEqualTo("1°");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testDivision() {
        Expression expression = new Expression("1 / -2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("-0.5");
        Assertions.assertThat(expression.toString()).isEqualTo("1 / -2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testExponential() {
        Expression expression = new Expression("4 ^ 3 ^ 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 6);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("262144");
        Assertions.assertThat(expression.toString()).isEqualTo("4 ^ 3 ^ 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 6);

        Assertions.assertThat(new Expression("4^0").<BigDecimal>evaluate()).isEqualByComparingTo("1");
    }

    @Test
    public void testFactorial() {
        Expression expression = new Expression("5!");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("120");
        Assertions.assertThat(expression.toString()).isEqualTo("5!");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testModulo() {
        Expression expression = new Expression("5 % 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1");
        Assertions.assertThat(expression.toString()).isEqualTo("5 % 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testModulus() {
        Expression expressionPositive = new Expression("|5|");
        VerifyExpressionsTools.checkWarmUpCache(expressionPositive, 3);
        Assertions.assertThat(expressionPositive.<BigDecimal>evaluate()).isEqualByComparingTo("5");
        Assertions.assertThat(expressionPositive.toString()).isEqualTo("|5|");
        VerifyExpressionsTools.commonVerifications(expressionPositive);
        VerifyExpressionsTools.checkCache(expressionPositive, 3);

        Expression expressionNegative = new Expression("|-5|");
        VerifyExpressionsTools.checkWarmUpCache(expressionNegative, 3);
        Assertions.assertThat(expressionNegative.<BigDecimal>evaluate()).isEqualByComparingTo("5");
        Assertions.assertThat(expressionNegative.toString()).isEqualTo("|-5|");
        VerifyExpressionsTools.commonVerifications(expressionNegative);
        VerifyExpressionsTools.checkCache(expressionNegative, 3);
    }

    @Test
    public void testMultiplication() {
        Expression expression = new Expression("-1 * 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("-2");
        Assertions.assertThat(expression.toString()).isEqualTo("-1 * 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testNegativeParenthesis() {
        Expression expression = new Expression("-(1)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("-1");
        Assertions.assertThat(expression.toString()).isEqualTo("-(1)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testNumberRounding() {
        Expression ceiling = new Expression("ceiling(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(ceiling, 4);
        Assertions.assertThat(ceiling.<BigDecimal>evaluate()).isEqualByComparingTo("1.56");
        Assertions.assertThat(ceiling.toString()).isEqualTo("ceiling(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(ceiling);
        VerifyExpressionsTools.checkCache(ceiling, 4);

        Expression down = new Expression("down(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(down, 4);
        Assertions.assertThat(down.<BigDecimal>evaluate()).isEqualByComparingTo("1.55");
        Assertions.assertThat(down.toString()).isEqualTo("down(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(down);
        VerifyExpressionsTools.checkCache(down, 4);

        Expression floor = new Expression("floor(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(floor, 4);
        Assertions.assertThat(floor.<BigDecimal>evaluate()).isEqualByComparingTo("1.55");
        Assertions.assertThat(floor.toString()).isEqualTo("floor(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(floor);
        VerifyExpressionsTools.checkCache(floor, 4);

        Expression halfDown = new Expression("halfDown(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(halfDown, 4);
        Assertions.assertThat(halfDown.<BigDecimal>evaluate()).isEqualByComparingTo("1.55");
        Assertions.assertThat(halfDown.toString()).isEqualTo("halfDown(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(halfDown);
        VerifyExpressionsTools.checkCache(halfDown, 4);

        Expression halfEven = new Expression("halfEven(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(halfEven, 4);
        Assertions.assertThat(halfEven.<BigDecimal>evaluate()).isEqualByComparingTo("1.56");
        Assertions.assertThat(halfEven.toString()).isEqualTo("halfEven(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(halfEven);
        VerifyExpressionsTools.checkCache(halfEven, 4);

        Expression halfUp = new Expression("halfUp(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(halfUp, 4);
        Assertions.assertThat(halfUp.<BigDecimal>evaluate()).isEqualByComparingTo("1.56");
        Assertions.assertThat(halfUp.toString()).isEqualTo("halfUp(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(halfUp);
        VerifyExpressionsTools.checkCache(halfUp, 4);

        Expression up = new Expression("up(1.555, 2)");
        VerifyExpressionsTools.checkWarmUpCache(up, 4);
        Assertions.assertThat(up.<BigDecimal>evaluate()).isEqualByComparingTo("1.56");
        Assertions.assertThat(up.toString()).isEqualTo("up(1.555, 2)");
        VerifyExpressionsTools.commonVerifications(up);
        VerifyExpressionsTools.checkCache(up, 4);
    }

    @Test
    public void testPercentual() {
        Expression expression = new Expression("10%");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("0.1");
        Assertions.assertThat(expression.toString()).isEqualTo("10%");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testProductOfSequence() {
        Expression exp1 = new Expression("P[[1,2,3]](P + 1)");
        VerifyExpressionsTools.checkWarmUpCache(exp1, 5);
        Assertions.assertThat(exp1.<BigDecimal>evaluate()).isEqualByComparingTo("24");
        Assertions.assertThat(exp1.toString()).isEqualTo("P[x](P + 1)");
        VerifyExpressionsTools.commonVerifications(exp1);
        VerifyExpressionsTools.checkCache(exp1, 5);

        Expression exp2 = new Expression("P[[1,2,3]](P + P[[1,2,3]](P + 2))");
        VerifyExpressionsTools.checkWarmUpCache(exp2, 9);
        Assertions.assertThat(exp2.<BigDecimal>evaluate()).isEqualByComparingTo("238266");
        Assertions.assertThat(exp2.toString()).isEqualTo("P[x](P + P[x](P + 2))");
        VerifyExpressionsTools.commonVerifications(exp2);
        VerifyExpressionsTools.checkCache(exp2, 9);

        Expression exp3 = new Expression("P[[1,2,3]](P + P + P[[1,2,3]](P + 2))");
        VerifyExpressionsTools.checkWarmUpCache(exp3, 9);
        Assertions.assertThat(exp3.<BigDecimal>evaluate()).isEqualByComparingTo("261888");
        Assertions.assertThat(exp3.toString()).isEqualTo("P[x](P + P + P[x](P + 2))");
        VerifyExpressionsTools.commonVerifications(exp3);
        VerifyExpressionsTools.checkCache(exp3, 9);
    }

    @Test
    public void testRoot() {
        Expression expression = new Expression("2 root 4");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("2");
        Assertions.assertThat(expression.toString()).isEqualTo("2√(4)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testSquareRoot() {
        Expression expression = new Expression("sqrt(144)");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("12");
        Assertions.assertThat(expression.toString()).isEqualTo("√(144)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testSubtraction() {
        Expression expression = new Expression("1 - 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("-1");
        Assertions.assertThat(expression.toString()).isEqualTo("1 - 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testSummation() {
        Expression exp1 = new Expression("S[[1,2,3]](S + 2)");
        VerifyExpressionsTools.checkWarmUpCache(exp1, 5);
        Assertions.assertThat(exp1.<BigDecimal>evaluate()).isEqualByComparingTo("12");
        Assertions.assertThat(exp1.toString()).isEqualTo("S[x](S + 2)");
        VerifyExpressionsTools.commonVerifications(exp1);
        VerifyExpressionsTools.checkCache(exp1, 5);

        Expression exp2 = new Expression("S[[1,3]](S + S[[1,3]](S + 2))");
        VerifyExpressionsTools.checkWarmUpCache(exp2, 7);
        Assertions.assertThat(exp2.<BigDecimal>evaluate()).isEqualByComparingTo("20");
        Assertions.assertThat(exp2.toString()).isEqualTo("S[x](S + S[x](S + 2))");
        VerifyExpressionsTools.commonVerifications(exp2);
        VerifyExpressionsTools.checkCache(exp2, 7);

        Expression exp3 = new Expression("S[[1,3]](S + S + S[[1,3]](S + 2))");
        VerifyExpressionsTools.checkWarmUpCache(exp3, 7);
        Assertions.assertThat(exp3.<BigDecimal>evaluate()).isEqualByComparingTo("24");
        Assertions.assertThat(exp3.toString()).isEqualTo("S[x](S + S + S[x](S + 2))");
        VerifyExpressionsTools.commonVerifications(exp3);
        VerifyExpressionsTools.checkCache(exp3, 7);
    }

    @Test
    public void testBinaryLogarithm() {
        Expression expression = new Expression("lb(5)", OPTIONS);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("2.32192809");
        Assertions.assertThat(expression.toString()).isEqualTo("lb(5)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testCommonLogarithm() {
        Expression expression = new Expression("log10(11)", OPTIONS);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1.04139269");
        Assertions.assertThat(expression.toString()).isEqualTo("log10(11)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

    @Test
    public void testLogarithm() {
        Expression expression = new Expression("log(3.4322, 50)", OPTIONS);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3.17224975");
        Assertions.assertThat(expression.toString()).isEqualTo("log(3.4322, 50)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
    }

    @Test
    public void testNaturalLogarithm() {
        Expression expression = new Expression("ln(5)", OPTIONS);
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1.60943791");
        Assertions.assertThat(expression.toString()).isEqualTo("ln(5)");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);
    }

}
