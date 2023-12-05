/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2022-2023 Marcelo Silva Portilho
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

import com.runestone.converters.DataConversionService;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.OperationVisitor;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;

public class VectorValueOperation extends AbstractVariableValueOperation {

    private final AbstractOperation[] vector;

    public VectorValueOperation(String variableName, AbstractOperation[] vector) {
        super(variableName);
        this.vector = vector;
    }

    @Override
    protected Object resolve(OperationContext context) {
        DataConversionService conversionService = context.conversionService();
        Object[] result = new Object[vector.length];
        for (int i = 0, splitLength = vector.length; i < splitLength; i++) {
            Class<?> type = vector[i] instanceof AssignedVariableOperation ? vector[i].getExpectedType() : getExpectedType().getComponentType();
            Object value = vector[i].evaluate(context);
            if (value == null) {
                return null;
            }
            result[i] = conversionService.convert(value, type);
        }
        return result;
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        AbstractOperation[] vector = new AbstractOperation[this.vector.length];
        for (int i = 0, totalLength = this.vector.length; i < totalLength; i++) {
            vector[i] = this.vector[i].copy(context);
        }
        return new VectorValueOperation(getVariableName(), vector);
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        builder.append(formatValue(vector));
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        for (AbstractOperation abstractOperation : vector) {
            abstractOperation.accept(visitor);
        }
        visitor.visit(this);
    }
}
