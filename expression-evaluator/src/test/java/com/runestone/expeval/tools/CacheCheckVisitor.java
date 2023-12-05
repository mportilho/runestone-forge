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

public class CacheCheckVisitor implements OperationVisitor<Integer> {

    private Set<AbstractOperation> visitedVariables = new HashSet<>();
    private int counter = 0;

    public CacheCheckVisitor reset() {
        visitedVariables = new HashSet<>();
        counter = 0;
        return this;
    }

    @Override
    public Integer getFinalResult() {
        return counter;
    }

    private Integer checkCache(AbstractOperation operation) {
        if (visitedVariables.contains(operation)) {
            return counter;
        } else {
            visitedVariables.add(operation);
        }

        if (operation.getCache() != null) {
            return ++counter;
        }
        return counter;
    }

    @Override
    public Integer visit(BaseOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AbstractUnaryOperator operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AbstractBinaryOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AbstractDateTimeOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(DecisionOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(FunctionOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AbstractSequencialMathOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AssignedVariableOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AbstractConstantValueOperation operation) {
        return checkCache(operation);
    }

    @Override
    public Integer visit(AbstractVariableValueOperation operation) {
        return checkCache(operation);
    }
}
