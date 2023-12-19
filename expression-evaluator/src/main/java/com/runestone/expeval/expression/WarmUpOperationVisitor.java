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

package com.runestone.expeval.expression;

import com.runestone.expeval.operation.*;
import com.runestone.expeval.operation.datetime.AbstractDateTimeOperation;
import com.runestone.expeval.operation.math.AbstractSequencialMathOperation;
import com.runestone.expeval.operation.other.DecisionOperation;
import com.runestone.expeval.operation.other.FunctionOperation;
import com.runestone.expeval.operation.values.AbstractConstantValueOperation;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.operation.values.variable.AssignedVariableOperation;

import java.util.*;

/**
 * Warm up visitor that evaluates all operations that can be evaluated
 *
 * @author Marcelo Portilho
 */
public class WarmUpOperationVisitor implements OperationVisitor<Object> {

    private final OperationContext context;

    public WarmUpOperationVisitor(OperationContext context) {
        this.context = context;
    }

    private boolean canEvaluate(Collection<? extends AbstractOperation> operations) {
        for (AbstractOperation operation : operations) {
            if (!canEvaluate(operation)) {
                return false;
            }
        }
        return true;
    }

    private boolean canEvaluate(Map<String, ? extends AbstractOperation> operations) {
        return canEvaluate(operations.values());
    }

    private boolean canEvaluate(AbstractOperation operation) {
        return Objects.nonNull(operation) && Objects.nonNull(operation.getCache());
    }

    @Override
    public Object visit(BaseOperation operation) {
        return canEvaluate(operation.getAssignedVariables()) && canEvaluate(operation.getOperation()) ?
                operation.evaluate(context) : null;
    }

    @Override
    public Object visit(AbstractUnaryOperator operation) {
        return canEvaluate(operation.getOperand()) ? operation.evaluate(context) : null;
    }

    @Override
    public Object visit(AbstractBinaryOperation operation) {
        return canEvaluate(operation.getLeftOperand()) && canEvaluate(operation.getRightOperand()) ?
                operation.evaluate(context) : null;
    }

    @Override
    public Object visit(AbstractDateTimeOperation operation) {
        return canEvaluate(operation.getLeftOperand()) && canEvaluate(operation.getRightOperand()) ?
                operation.evaluate(context) : null;
    }

    @Override
    public Object visit(DecisionOperation operation) {
        try {
            return operation.evaluate(context);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public Object visit(FunctionOperation operation) {
        List<AbstractOperation> list = Arrays.asList(operation.getParameters());
        return canEvaluate(list) && context.getFunction(operation.getFunctionName(), list.size()) != null ?
                operation.evaluate(context) : null;
    }

    @Override
    public Object visit(AbstractSequencialMathOperation operation) {
        return canEvaluate(operation.getInput()) ? operation.evaluate(context) : null;
    }

    @Override
    public Object visit(AssignedVariableOperation operation) {
        return canEvaluate(operation.getAssignedOperation()) ? operation.evaluate(context) : null;
    }

    @Override
    public Object visit(AbstractConstantValueOperation operation) {
        return operation.evaluate(context);
    }

    @Override
    public Object visit(AbstractVariableValueOperation operation) {
        try {
            return operation.evaluate(context);
        } catch (NullPointerException e) {
            return null;
        }
    }

}
