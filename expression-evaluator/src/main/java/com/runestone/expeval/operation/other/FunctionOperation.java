/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2021-2022. Marcelo Silva Portilho
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.runestone.expeval.operation.other;

import com.runestone.expeval.exceptions.ExpressionEvaluatorException;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.OperationVisitor;
import com.runestone.expeval.support.callsite.CallSiteContext;
import com.runestone.expeval.support.callsite.OperationCallSite;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

public class FunctionOperation extends AbstractOperation {

    private final String functionName;
    private final AbstractOperation[] parameters;
    private final boolean cacheHint;

    public FunctionOperation(String functionName, AbstractOperation[] parameters, boolean caching) {
        this.functionName = functionName;
        this.parameters = Objects.requireNonNull(parameters, "Parameters cannot be null");
        for (AbstractOperation parameter : this.parameters) {
            parameter.addParent(this);
        }
        this.cacheHint = caching;
    }

    @Override
    protected Object resolve(OperationContext context) {
        OperationCallSite caller = context.getFunction(functionName, parameters.length);
        if (caller == null) {
            throw new ExpressionEvaluatorException(String.format("Function [%s] with [%s] parameter(s) not found", functionName, parameters.length));
        }

        Object[] params = new Object[parameters.length];
        for (int i = 0, paramsLength = params.length; i < paramsLength; i++) {
            params[i] = parameters[i].evaluate(context);
        }
        CallSiteContext callSiteContext = new CallSiteContext(context.mathContext(), context.scale(), context.zoneId(), context.currentDateTime());
        Object result = caller.call(callSiteContext, params, context.conversionService()::convert);
        expectedTypeByValue(result, caller.getMethodType().returnType());
        return convertToInternalTypes(result, context);
    }

    private Object convertToInternalTypes(Object value, OperationContext context) {
        if (value instanceof LocalDateTime dateTime) {
            return ZonedDateTime.of(dateTime, context.zoneId());
        }
        return value;
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        AbstractOperation[] copyParams = new AbstractOperation[parameters.length];
        for (int i = 0, copyParamsLength = copyParams.length; i < copyParamsLength; i++) {
            copyParams[i] = parameters[i].copy(context);
        }
        return new FunctionOperation(functionName, copyParams, isCaching());
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        if (!cacheHint) {
            builder.append("$.");
        }
        builder.append(getFunctionName()).append("(");
        int index = parameters.length;
        for (AbstractOperation parameter : parameters) {
            parameter.toString(builder);
            if (--index != 0) {
                builder.append(", ");
            }
        }
        builder.append(")");
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        for (AbstractOperation parameter : getParameters()) {
            parameter.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public boolean getCacheHint() {
        return this.cacheHint;
    }

    public AbstractOperation[] getParameters() {
        return parameters;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    protected String getOperationToken() {
        return "";
    }

}
