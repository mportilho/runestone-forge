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
