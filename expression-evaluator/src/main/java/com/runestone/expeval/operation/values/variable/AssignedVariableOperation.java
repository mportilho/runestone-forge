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
import com.runestone.expeval.operation.OperationVisitor;

public class AssignedVariableOperation extends AbstractOperation {

    private final String variableName;
    private final AbstractOperation assignedOperation;

    public AssignedVariableOperation(String variableName, AbstractOperation assignedOperation) {
        this.variableName = variableName;
        this.assignedOperation = assignedOperation;
        assignedOperation.addParent(this);
    }

    @Override
    protected Object resolve(OperationContext context) {
        Object value = assignedOperation.evaluate(context);
        expectedTypeByValue(value, getExpectedType());
        return value;
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        AssignedVariableOperation copyOperation = new AssignedVariableOperation(getVariableName(), assignedOperation.copy(context));
        context.getAssignedVariables().put(getVariableName(), copyOperation);
        return copyOperation;
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        this.assignedOperation.accept(visitor);
        visitor.visit(this);
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        builder.append(getCache() != null ? formatValue(getCache()) : getVariableName());
    }

    public String getVariableName() {
        return variableName;
    }

    public AbstractOperation getAssignedOperation() {
        return assignedOperation;
    }

    @Override
    public String getOperationToken() {
        return ":=";
    }

}
