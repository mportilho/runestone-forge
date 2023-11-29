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
