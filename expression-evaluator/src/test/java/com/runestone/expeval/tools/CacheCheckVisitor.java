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
