/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2021-2022. Marcelo Silva Portilho
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.runestone.expeval.operation;

import java.util.Objects;

public abstract class AbstractUnaryOperator extends AbstractOperation {

    private final AbstractOperation operand;
    private final OperatorPosition operatorPosition;

    public AbstractUnaryOperator(AbstractOperation operand, OperatorPosition operatorPosition) {
        this.operand = operand;
        this.operatorPosition = Objects.requireNonNull(operatorPosition, "Operator token position of unary operation is required");
        this.operand.addParent(this);
    }

    public AbstractOperation getOperand() {
        return operand;
    }

    @Override
    public void formatRepresentation(StringBuilder builder) {
        switch (operatorPosition) {
            case FUNCTION -> {
                builder.append(getOperationToken()).append('(');
                getOperand().toString(builder);
                builder.append(')');
            }
            case LEFT -> {
                builder.append(getOperationToken());
                getOperand().toString(builder);
            }
            case RIGHT -> {
                getOperand().toString(builder);
                builder.append(getOperationToken());
            }
            case WRAPPED -> {
                builder.append(getOperationToken());
                getOperand().toString(builder);
                builder.append(getOperationToken());
            }
            default -> throw new IllegalStateException("Operation position not implemented: " + operatorPosition);
        }
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        getOperand().accept(visitor);
        visitor.visit(this);
    }

    public enum OperatorPosition {
        LEFT, RIGHT, WRAPPED, FUNCTION
    }
}
