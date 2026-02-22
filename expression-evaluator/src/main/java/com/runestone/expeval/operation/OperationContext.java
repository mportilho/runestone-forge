/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2021-2023 Marcelo Silva Portilho
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

package com.runestone.expeval.operation;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.support.callsite.OperationCallSite;

import java.math.MathContext;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.function.Supplier;

/**
 * Context for evaluating operations
 *
 * @param mathContext       math context
 * @param scale             scale for calculations. Can be null
 * @param allowingNull      allowing null as evaluation result on operations. Null results are not allowed by default and cancels the evaluation process
 * @param currentDateTime   current date time supplier
 * @param conversionService conversion service
 * @param expressionContext expression context for evaluating expressions
 * @param userContext       user context for evaluating expressions. It has precedence over expression context
 * @param zoneId            zone id for date time operations
 * @author Marcelo Portilho
 */
public record OperationContext(

        MathContext mathContext,
        Integer scale,
        boolean allowingNull,
        Supplier<Temporal> currentDateTime,
        DataConversionService conversionService,
        ExpressionContext expressionContext,
        ExpressionContext userContext,
        ZoneId zoneId
) {

    public OperationCallSite getFunction(String name, int parameterCount) {
        String functionKey = OperationCallSite.keyName(name, parameterCount);
        String fallbackFunctionKey = parameterCount > 1 ? OperationCallSite.keyName(name, 1) : null;
        return getFunction(functionKey, fallbackFunctionKey);
    }

    public OperationCallSite getFunction(String functionKey, String fallbackFunctionKey) {
        OperationCallSite func = userContext.findFunction(functionKey);
        if (func != null) {
            return func;
        }

        if (userContext == expressionContext) {
            if (fallbackFunctionKey == null) {
                return null;
            }
            return userContext.findFunction(fallbackFunctionKey);
        }

        func = expressionContext.findFunction(functionKey);
        if (func != null || fallbackFunctionKey == null) {
            return func;
        }

        func = userContext.findFunction(fallbackFunctionKey);
        if (func != null) {
            return func;
        }

        return expressionContext.findFunction(fallbackFunctionKey);
    }

}
