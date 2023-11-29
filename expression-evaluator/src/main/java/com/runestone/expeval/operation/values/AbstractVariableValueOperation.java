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

package com.runestone.expeval.operation.values;

import com.runestone.expeval.exceptions.ExpressionConfigurationException;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.OperationVisitor;

import java.util.function.Supplier;

public abstract class AbstractVariableValueOperation extends AbstractOperation {

    private final String variableName;
    private Object value;

    public AbstractVariableValueOperation(String variableName) {
        this.variableName = variableName;
    }

    @Override
    protected String getOperationToken() {
        return "";
    }

    /**
     * Sets a new value for this variable overriding the current value without any checks, validations or cache resets
     *
     * @param value the new value
     */
    protected void overrideValue(Object value) {
        this.value = value;
    }

    /**
     * Sets a new value
     *
     * @param newValue the new value
     * @throws ExpressionConfigurationException if the variable is locked or the new value is null
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setValue(Object newValue) {
        if (newValue == null) {
            throw new ExpressionConfigurationException(String.format("Variable [%s] received a null value", variableName));
        }
        if (value == null || (!value.equals(newValue) || (value.getClass().isInstance(newValue) && value instanceof Comparable v1 && v1.compareTo(newValue) != 0))) {
            this.value = newValue;
            clearCache();
        }
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        if (getCache() != null) {
            builder.append(formatValue(getCache()));
        } else {
            if (getValue() == null || getValue() instanceof VariableProvider || getValue() instanceof Supplier<?>) {
                builder.append(getVariableName());
            } else if (String.class.equals(getExpectedType())) {
                builder.append('\'').append(getValue()).append('\'');
            } else {
                builder.append(getValue());
            }
        }
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        visitor.visit(this);
    }

    public String getVariableName() {
        return variableName;
    }

    public Object getValue() {
        return value;
    }

}
