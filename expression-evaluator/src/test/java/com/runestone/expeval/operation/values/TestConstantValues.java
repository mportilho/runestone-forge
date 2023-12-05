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
