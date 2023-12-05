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

package com.runestone.expeval.operation.logic;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLogicOperations {

    @Test
    public void testAnd() {
        Expression expression = new Expression("true and false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true and false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("true and true").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("false and false").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("false and true").<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testEquals() {
        Expression expression = new Expression("true = false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true = false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("1 = 2").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("'str1' = 'str1'").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("2000-01-01 = 2000-04-05").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("12:00:00 = 12:00:00").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("2000-01-01T12:00:00 = 2000-01-01T12:00:00").<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testGreater() {
        Expression expression = new Expression("true > false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true > false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("1 > 2").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("'str1' > 'str1'").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("2000-01-01 > 2000-04-05").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("12:00:00 > 12:00:00").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("2000-01-01T12:00:00 > 2000-01-01T12:00:00").<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testGreaterOrEquals() {
        Expression expression = new Expression("true >= false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true >= false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("1 >= 2").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("'str1' >= 'str1'").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("2000-01-01 >= 2000-04-05").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("12:00:00 >= 12:00:00").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("2000-01-01T12:00:00 >= 2000-01-01T12:00:00").<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testLess() {
        Expression expression = new Expression("true < false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true < false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("1 < 2").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("'str1' < 'str1'").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("2000-01-01 < 2000-04-05").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("12:00:00 < 12:00:00").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("2000-01-01T12:00:00 < 2000-01-01T12:00:00").<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testLessOrEquals() {
        Expression expression = new Expression("true <= false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true <= false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("1 <= 2").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("'str1' <= 'str1'").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("2000-01-01 <= 2000-04-05").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("12:00:00 <= 12:00:00").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("2000-01-01T12:00:00 <= 2000-01-01T12:00:00").<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testNand() {
        Expression expression = new Expression("true nand false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true nand false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("true nand true").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("false nand false").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("false nand true").<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testNegation() {
        Expression expression = new Expression("!true");
        VerifyExpressionsTools.checkWarmUpCache(expression, 3);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("!true");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);

        Assertions.assertThat(new Expression("!false").<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testNor() {
        Expression expression = new Expression("true nor false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true nor false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);

        Assertions.assertThat(new Expression("true nor true").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("false nor false").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("false nor true").<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testNotEquals() {
        Expression expression = new Expression("true != false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true != false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("1 != 2").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("'str1' != 'str1'").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("2000-01-01 != 2000-04-05").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("12:00:00 != 12:00:00").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("2000-01-01T12:00:00 != 2000-01-01T12:00:00").<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testOr() {
        Expression expression = new Expression("true or false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true or false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 3);

        Assertions.assertThat(new Expression("true or true").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("false or false").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("false or true").<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testXnor() {
        Expression expression = new Expression("true xnor false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true xnor false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("true xnor true").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("false xnor false").<Boolean>evaluate()).isTrue();
        Assertions.assertThat(new Expression("false xnor true").<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testXor() {
        Expression expression = new Expression("true xor false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("true xor false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);

        Assertions.assertThat(new Expression("true xor true").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("false xor false").<Boolean>evaluate()).isFalse();
        Assertions.assertThat(new Expression("false xor true").<Boolean>evaluate()).isTrue();
    }

}
