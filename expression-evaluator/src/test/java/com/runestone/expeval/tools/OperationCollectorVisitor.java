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

package com.runestone.expeval.tools;

import com.runestone.expeval.operation.*;
import com.runestone.expeval.operation.datetime.AbstractDateTimeOperation;
import com.runestone.expeval.operation.math.AbstractSequencialMathOperation;
import com.runestone.expeval.operation.other.DecisionOperation;
import com.runestone.expeval.operation.other.FunctionOperation;
import com.runestone.expeval.operation.values.AbstractConstantValueOperation;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.operation.values.variable.AssignedVariableOperation;

import java.util.HashSet;
import java.util.Set;

public class OperationCollectorVisitor implements OperationVisitor<Set<AbstractOperation>> {

    private final Set<AbstractOperation> operations = new HashSet<>();

    @Override
    public Set<AbstractOperation> getFinalResult() {
        return operations;
    }

    @Override
    public Set<AbstractOperation> visit(BaseOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AbstractUnaryOperator operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AbstractBinaryOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AbstractDateTimeOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(DecisionOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(FunctionOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AbstractSequencialMathOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AssignedVariableOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AbstractConstantValueOperation operation) {
        operations.add(operation);
        return null;
    }

    @Override
    public Set<AbstractOperation> visit(AbstractVariableValueOperation operation) {
        operations.add(operation);
        return null;
    }
}
