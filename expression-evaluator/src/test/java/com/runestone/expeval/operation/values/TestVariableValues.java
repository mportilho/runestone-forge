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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TestVariableValues {

    @Test
    public void testNumberVariables() {
        Expression expression = new Expression("a + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("a + 2");
        expression.setVariable("a", 1);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testNegativeNumberVariables() {
        Expression expression = new Expression("-a + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("-a + 2");
        expression.setVariable("a", 1);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1");
        Assertions.assertThat(expression.toString()).isEqualTo("-1 + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 5);
        VerifyExpressionsTools.checkWarmUpCache(expression, 5);
    }

    @Test
    public void testBooleanVariables() {
        Expression expression = new Expression("a and false");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("a and false");
        expression.setVariable("a", true);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("true and false");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testStringVariables() {
        Expression expression = new Expression("a = 'b'");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("a = 'b'");
        expression.setVariable("a", "1");
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.toString()).isEqualTo("'1' = 'b'");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testDateVariablesWithImplicitTypeConversion() {
        Expression expression = new Expression("a = 2000-01-01");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("a = 2000-01-01");
        expression.setVariable("a", "2000-01-01");
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("2000-01-01 = 2000-01-01");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testTimeVariables() {
        Expression expression = new Expression("a = 12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("a = 12:00:00");
        expression.setVariable("a", "12:00:00");
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("12:00 = 12:00");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testDateTimeVariables() {
        Expression expression = new Expression("a = 2000-01-01T12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        Assertions.assertThat(expression.toString()).isEqualTo("a = 2000-01-01T12:00:00");
        expression.setVariable("a", ZonedDateTime.of(LocalDate.parse("2000-01-01"), LocalTime.parse("12:00:00"), expression.getOptions().zoneId()));
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("2000-01-01T12:00:00 = 2000-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testVariableSupplier() {
        Expression expression = new Expression("a + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        expression.setVariableProvider("a", c -> 1);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testGenericVariableSupplier() {
        Expression expression = new Expression("a + 2");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);
        expression.setVariableProvider("a", () -> 1);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    @Test
    public void testVariableProviderCurrentDateTimeConsistencyPerEvaluation() throws Exception {
        Expression expression = new Expression("a = b");
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);

        AtomicReference<VariableValueProviderContext> firstEvalCtxA = new AtomicReference<>();
        AtomicReference<VariableValueProviderContext> firstEvalCtxB = new AtomicReference<>();
        AtomicReference<Temporal> firstEvalA = new AtomicReference<>();
        AtomicReference<Temporal> firstEvalB = new AtomicReference<>();
        expression.setVariableProvider("a", context -> storeProviderAndCurrentDateTime(context, firstEvalCtxA, firstEvalA));
        expression.setVariableProvider("b", context -> storeProviderAndCurrentDateTime(context, firstEvalCtxB, firstEvalB));

        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(firstEvalA.get()).isSameAs(firstEvalB.get());
        Assertions.assertThat(firstEvalCtxA.get()).isSameAs(firstEvalCtxB.get());

        Thread.sleep(2);

        AtomicReference<VariableValueProviderContext> secondEvalCtxA = new AtomicReference<>();
        AtomicReference<VariableValueProviderContext> secondEvalCtxB = new AtomicReference<>();
        AtomicReference<Temporal> secondEvalA = new AtomicReference<>();
        AtomicReference<Temporal> secondEvalB = new AtomicReference<>();
        expression.setVariableProvider("a", context -> storeProviderAndCurrentDateTime(context, secondEvalCtxA, secondEvalA));
        expression.setVariableProvider("b", context -> storeProviderAndCurrentDateTime(context, secondEvalCtxB, secondEvalB));

        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(secondEvalA.get()).isSameAs(secondEvalB.get());
        Assertions.assertThat(secondEvalCtxA.get()).isSameAs(secondEvalCtxB.get());
        Assertions.assertThat(secondEvalCtxA.get()).isNotSameAs(firstEvalCtxA.get());
        Assertions.assertThat(secondEvalA.get()).isNotSameAs(firstEvalA.get());
    }

    @Test
    public void testVariableMapWithSuppliers() {
        Expression expression = new Expression("a + b + c + 4");
        VerifyExpressionsTools.checkWarmUpCache(expression, 1);

        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 1);
        variables.put("b", (Supplier<Object>) () -> 2);
        variables.put("c", (VariableProvider) c -> 3);
        expression.setVariables(variables);

        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("10");
        Assertions.assertThat(expression.toString()).isEqualTo("1 + 2 + 3 + 4");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 8);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
    }

    @Test
    public void testVariableArray() {
        Expression exp1 = new Expression("max(array)");
        VerifyExpressionsTools.checkWarmUpCache(exp1, 0);
        exp1.setVariable("array", new int[]{1, 2, 3});
        Assertions.assertThat(exp1.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(exp1.toString()).isEqualTo("max([1, 2, 3])");
        VerifyExpressionsTools.commonVerifications(exp1);
        VerifyExpressionsTools.checkCache(exp1, 3);
        VerifyExpressionsTools.checkWarmUpCache(exp1, 3);

        Expression exp2 = new Expression("max(array)");
        VerifyExpressionsTools.checkWarmUpCache(exp2, 0);
        exp2.setVariable("array", new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(5)});
        Assertions.assertThat(exp2.<BigDecimal>evaluate()).isEqualByComparingTo("5");
        Assertions.assertThat(exp2.toString()).isEqualTo("max([1, 2 .. 4, 5])");
        VerifyExpressionsTools.commonVerifications(exp2);
        VerifyExpressionsTools.checkCache(exp2, 3);
        VerifyExpressionsTools.checkWarmUpCache(exp2, 3);
    }

    @Test
    public void testUndefinedTypeVariables() {
        Expression expression = new Expression("a = b");
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);
        expression.setVariable("a", "value");
        expression.setVariable("b", "value");
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.toString()).isEqualTo("'value' = 'value'");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 4);
        VerifyExpressionsTools.checkWarmUpCache(expression, 4);
    }

    private static Temporal storeProviderAndCurrentDateTime(
            VariableValueProviderContext context,
            AtomicReference<VariableValueProviderContext> contextReference,
            AtomicReference<Temporal> temporalReference
    ) {
        contextReference.set(context);
        Temporal currentDateTime = context.currentDateTime().get();
        temporalReference.set(currentDateTime);
        return currentDateTime;
    }

}
