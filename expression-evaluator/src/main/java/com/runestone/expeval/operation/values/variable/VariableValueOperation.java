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

package com.runestone.expeval.operation.values.variable;

import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.operation.values.VariableProvider;
import com.runestone.expeval.operation.values.VariableValueProviderContext;

public class VariableValueOperation extends AbstractVariableValueOperation {

    public VariableValueOperation(String variableName) {
        super(variableName);
    }

    @Override
    protected Object resolve(OperationContext context) {
        Object result = getValue();
        if (result != null) {
            return unwrapFunction(result, context);
        } else {
            Object currValue = context.userContext().findValue(getVariableName());
            if (currValue == null) {
                currValue = context.expressionContext().findValue(getVariableName());
            }
            return unwrapFunction(currValue, context);
        }
    }

    private Object unwrapFunction(Object object, OperationContext context) {
        if (object instanceof VariableProvider variableProvider) {
            var providerContext = new VariableValueProviderContext(context.mathContext(), context.scale(), context.zoneId(), context.currentDateTime());
            return variableProvider.retrieveValue(providerContext);
        } else {
            return object;
        }
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        VariableValueOperation copiedOperation = new VariableValueOperation(this.getVariableName());
        copiedOperation.overrideValue(this.getValue());
        context.getVariables().put(this.getVariableName(), copiedOperation);
        return copiedOperation;
    }

}
