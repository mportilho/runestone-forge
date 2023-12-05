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

package com.runestone.expeval.expression.supplier;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.expression.ExpressionOptions;
import com.runestone.memoization.MemoizedExpiringFunction;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A default implementation of {@link ExpressionSupplier} that caches newly created expressions for 10 minutes by default
 *
 * @author Marcelo Portilho
 */
public class DefaultExpressionSupplier implements ExpressionSupplier {

    private final ExpressionOptions options;
    private final ExpressionContext expressionContext;
    private final MemoizedExpiringFunction<String, Expression> cache;

    /**
     * Creates a new instance of {@link DefaultExpressionSupplier} with the expression options and context that will be used to
     * create new expressions
     *
     * @param options           the expression options
     * @param expressionContext the expression context
     */
    public DefaultExpressionSupplier(ExpressionOptions options, ExpressionContext expressionContext) {
        this.options = Objects.requireNonNull(options, "Expression options cannot be null");
        this.expressionContext = Objects.requireNonNull(expressionContext, "Expression context cannot be null");
        cache = new MemoizedExpiringFunction<>(this::generateNewExpression, 10, TimeUnit.MINUTES);
    }

    @Override
    public Expression createExpression(String expression) {
        return cache.apply(expression).copy();
    }

    private Expression generateNewExpression(String expression) {
        return new Expression(expression, options, expressionContext).warmUp();
    }

}
