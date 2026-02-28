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

package com.runestone.expeval.operation;

import com.runestone.expeval.exceptions.ExpressionEvaluatorException;
import com.runestone.expeval.operation.values.variable.AssignedVariableOperation;
import com.runestone.expeval.operation.values.variable.AssignedVectorOperation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Objects.nonNull;

public class BaseOperation extends AbstractOperation {

    private final Map<String, AssignedVariableOperation> assignedVariables;
    private final AbstractOperation operation;
    private final boolean hasAssignedVariables;

    public BaseOperation(AbstractOperation operation, Map<String, AssignedVariableOperation> assignedVariables) {
        this.assignedVariables = assignedVariables != null ? assignedVariables : Collections.emptyMap();
        this.hasAssignedVariables = !this.assignedVariables.isEmpty();
        this.operation = operation;
        if (this.operation != null) {
            this.expectedType(this.operation.getExpectedType());
            this.operation.addParent(this);
        } else {
            this.expectedType(Boolean.class);
        }
    }

    @Override
    protected Object resolve(OperationContext context) {
        if (hasAssignedVariables) {
            for (Entry<String, AssignedVariableOperation> entry : assignedVariables.entrySet()) {
                entry.getValue().evaluate(context);
            }
        }
        if (operation != null) {
            Object result = operation.evaluate(context);
            if (result instanceof Number number) {
                MathContext mathContext = context.mathContext();
                Integer scale = context.scale();
                BigDecimal numberResult;
                if (number instanceof BigDecimal bd) {
                    numberResult = bd;
                } else if (number instanceof Integer i) {
                    numberResult = new BigDecimal(i, mathContext);
                } else if (number instanceof Long l) {
                    numberResult = new BigDecimal(l, mathContext);
                } else {
                    numberResult = new BigDecimal(number.toString(), mathContext);
                }

                if (scale != null) {
                    numberResult = numberResult.setScale(scale, mathContext.getRoundingMode());
                }
                return numberResult;
            }
            return result;
        }
        if (BigDecimal.class.equals(this.getExpectedType())) {
            return BigDecimal.ZERO;
        } else if (Boolean.class.equals(this.getExpectedType())) {
            return Boolean.FALSE;
        }
        throw new ExpressionEvaluatorException(String.format(
                "Type [%s] not expected for base operation. Must be number or boolean.", this.getExpectedType()));
    }

    @Override
    protected Object castOperationResult(Object result, OperationContext context) {
        if (result == null && !context.allowingNull()) {
            throw new NullPointerException(String.format("Invalid null result for expression [%s] ", this));
        }
        if (result != null && !getExpectedType().isInstance(result)) {
            this.expectedTypeByValue(result, getExpectedType());
        }
        return result;
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        Map<String, AssignedVariableOperation> newAssignedVariables = null;
        if (nonNull(assignedVariables)) {
            newAssignedVariables = new HashMap<>();
            for (Entry<String, AssignedVariableOperation> entry : assignedVariables.entrySet()) {
                newAssignedVariables.put(entry.getKey(), (AssignedVariableOperation) entry.getValue().copy(context));
            }
        }
        return new BaseOperation(operation != null ? operation.copy(context) : null, newAssignedVariables);
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        Set<Entry<String, AssignedVariableOperation>> entrySet = assignedVariables.entrySet();
        List<AssignedVectorOperation> vectorOperations = new ArrayList<>();
        AbstractOperation lastAssignedOperation = null;
        for (Entry<String, AssignedVariableOperation> entry : entrySet) {
            if (entry.getValue() instanceof AssignedVectorOperation vectorOperation) {
                if (lastAssignedOperation != null && lastAssignedOperation != vectorOperation.getAssignedOperation()) {
                    formatAssignedVectorOperation(builder, vectorOperations);
                }
                vectorOperations.add(vectorOperation);
                lastAssignedOperation = vectorOperation.getAssignedOperation();
            } else {
                formatAssignedVectorOperation(builder, vectorOperations);
                builder.append(entry.getValue().getVariableName()).append(' ')
                        .append(entry.getValue().getOperationToken()).append(' ');
                entry.getValue().getAssignedOperation().toString(builder);
                builder.append(";\n");
            }
        }
        formatAssignedVectorOperation(builder, vectorOperations);

        if (operation != null) {
            operation.toString(builder);
        } else if (!builder.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
        }
    }

    private static void formatAssignedVectorOperation(StringBuilder builder,
            List<AssignedVectorOperation> vectorOperations) {
        if (!vectorOperations.isEmpty()) {
            builder.append('[');
            for (int i = 0, commaLimit = vectorOperations.size() - 1; i < vectorOperations.size(); i++) {
                AssignedVectorOperation vector = vectorOperations.get(i);
                builder.append(vector.getVariableName());
                if (i < commaLimit) {
                    builder.append(", ");
                }
            }
            AssignedVectorOperation vectorOperation = vectorOperations.get(0);
            builder.append("] ").append(vectorOperation.getOperationToken()).append(' ')
                    .append(vectorOperation.getAssignedOperation());
            builder.append(";\n");
            vectorOperations.clear();
        }
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        getAssignedVariables().values().forEach(op -> op.accept(visitor));
        if (getOperation() != null) {
            getOperation().accept(visitor);
        }
        visitor.visit(this);
    }

    public AbstractOperation getOperation() {
        return operation;
    }

    public Map<String, AssignedVariableOperation> getAssignedVariables() {
        return assignedVariables;
    }

    @Override
    protected String getOperationToken() {
        return "";
    }

}
