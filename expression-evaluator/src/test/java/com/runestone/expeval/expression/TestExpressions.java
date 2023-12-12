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

package com.runestone.expeval.expression;

import com.runestone.expeval.support.callsite.OperationCallSite;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class TestExpressions {

    @Test
    public void testAddDictionaryAndFunction() {
        Expression expression = new Expression("adder(a, b)");
        expression.addDictionaryEntry("a", BigDecimal.ONE);
        expression.addDictionary(Map.of("b", BigDecimal.TEN));

        expression.addFunction("adder", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class),
                p -> ((BigDecimal) p[0]).add((BigDecimal) p[1]));

        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
    }

    @Test
    public void testAddOperationCallSite() {
        Expression expression = new Expression("adder(1, 10)");
        expression.addFunction(new OperationCallSite("adder", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class),
                p -> ((BigDecimal) p[0]).add((BigDecimal) p[1])));
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
    }

    @Test
    public void testUnknownTypeWithNumbers() {
        Expression expression = new Expression("a = b");
        expression.setVariable("a", 1);
        expression.setVariable("b", 2);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
    }

    @Test
    public void testUnknownTypeWithBooleans() {
        Expression expression = new Expression("a = b");
        expression.setVariable("a", true);
        expression.setVariable("b", true);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testUnknownTypeWithStrings() {
        Expression expression = new Expression("a = b");
        expression.setVariable("a", "abc");
        expression.setVariable("b", "abc");
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
    }

    @Test
    public void testUnknownTypeWithLocalDate() {
        LocalDate now = LocalDate.now();
        Expression expression = new Expression("a = b");
        expression.setVariable("a", now);
        expression.setVariable("b", now);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
    }

}
