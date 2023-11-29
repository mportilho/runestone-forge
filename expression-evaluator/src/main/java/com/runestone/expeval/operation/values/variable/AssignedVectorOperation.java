package com.runestone.expeval.operation.values.variable;


import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;

public class AssignedVectorOperation extends AssignedVariableOperation {

    private final int vectorPosition;

    public AssignedVectorOperation(String variableName, AbstractOperation assignedOperation, int vectorPosition) {
        super(variableName, assignedOperation);
        this.vectorPosition = vectorPosition;
    }

    @Override
    protected Object resolve(OperationContext context) {
        Object[] vector = getAssignedOperation().evaluate(context);
        return vector[vectorPosition];
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        AssignedVectorOperation copyOperation = new AssignedVectorOperation(getVariableName(), getAssignedOperation().copy(context), vectorPosition);
        context.getAssignedVariables().put(getVariableName(), copyOperation);
        return copyOperation;
    }

}
