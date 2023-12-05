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

package com.runestone.expeval.grammar.parser.impl;

import com.runestone.expeval.operation.*;
import com.runestone.expeval.operation.datetime.AbstractDateTimeOperation;
import com.runestone.expeval.operation.math.AbstractSequencialMathOperation;
import com.runestone.expeval.operation.other.DecisionOperation;
import com.runestone.expeval.operation.other.FunctionOperation;
import com.runestone.expeval.operation.values.AbstractConstantValueOperation;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.operation.values.variable.AssignedVariableOperation;

/**
 * This class is used to configure the initial state of an operation tree after it is parsed.
 *
 * @author Marcelo Portilho
 */
public class InitialConfigurationOperationVisitor implements OperationVisitor<Object> {

    private Object disableCaching(AbstractOperation operation) {
        if (!operation.getCacheHint()) {
            operation.configureCaching(false);
        }
        return operation;
    }

    @Override
    public Object visit(BaseOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AbstractUnaryOperator operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AbstractBinaryOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AbstractDateTimeOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(DecisionOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(FunctionOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AbstractSequencialMathOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AssignedVariableOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AbstractConstantValueOperation operation) {
        return disableCaching(operation);
    }

    @Override
    public Object visit(AbstractVariableValueOperation operation) {
        return disableCaching(operation);
    }
}
