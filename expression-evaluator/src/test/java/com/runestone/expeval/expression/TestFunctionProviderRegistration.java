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

import com.runestone.expeval.exceptions.ExpressionConfigurationException;
import com.runestone.expeval.exceptions.ExpressionEvaluatorException;
import com.runestone.expeval.expression.tool.DefaultCollisionFunctionProvider;
import com.runestone.expeval.expression.tool.MixedFunctionProvider;
import com.runestone.expeval.operation.other.tool.FunctionProviderClass;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestFunctionProviderRegistration {

    @Test
    void testClassProviderShouldRegisterOnlyStaticMethods() {
        Expression expression = new Expression("staticSum(2, 3)");
        expression.addFunctionsFrom(MixedFunctionProvider.class);

        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("5");

        Expression invalidExpression = new Expression("instanceMultiply(2, 3)");
        invalidExpression.addFunctionsFrom(MixedFunctionProvider.class);
        Assertions.assertThatThrownBy(invalidExpression::evaluate)
                .isExactlyInstanceOf(ExpressionEvaluatorException.class)
                .hasMessageContaining("Function [instanceMultiply] with [2] parameter(s) not found");
    }

    @Test
    void testInstanceProviderShouldRegisterStaticAndInstanceMethods() {
        Expression expression = new Expression("staticSum(2, 3) + instanceMultiply(2, 3)");
        expression.addFunctionsFrom(new MixedFunctionProvider());

        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
    }

    @Test
    void testShouldRejectProviderFunctionsThatOverrideDefaults() {
        Expression expression = new Expression("1");

        Throwable throwable = Assertions.catchThrowable(() -> expression.addFunctionsFrom(new DefaultCollisionFunctionProvider()));
        Assertions.assertThat(throwable).isExactlyInstanceOf(ExpressionConfigurationException.class);
        Assertions.assertThat(org.assertj.core.util.Throwables.getRootCause(throwable).getMessage()).contains("avg_1");
    }

    @Test
    void testShouldRejectProviderFunctionsThatOverrideExistingFunctions() {
        Expression expression = new Expression("extractedNumber()");
        expression.addFunctionsFrom(new FunctionProviderClass());

        Throwable throwable = Assertions.catchThrowable(() -> expression.addFunctionsFrom(new FunctionProviderClass()));
        Assertions.assertThat(throwable).isExactlyInstanceOf(ExpressionConfigurationException.class);
        Assertions.assertThat(org.assertj.core.util.Throwables.getRootCause(throwable).getMessage()).contains("extractedNumber_0");
    }
}

