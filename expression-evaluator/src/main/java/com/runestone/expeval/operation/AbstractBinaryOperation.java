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

public abstract class AbstractBinaryOperation extends AbstractOperation {

    private final AbstractOperation leftOperand;
    private final AbstractOperation rightOperand;

    public AbstractBinaryOperation(AbstractOperation leftOperand, AbstractOperation rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.leftOperand.addParent(this);
        this.rightOperand.addParent(this);
    }

    @Override
    public void formatRepresentation(StringBuilder builder) {
        getLeftOperand().toString(builder);
        builder.append(' ').append(getOperationToken()).append(' ');
        getRightOperand().toString(builder);
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        getLeftOperand().accept(visitor);
        getRightOperand().accept(visitor);
        visitor.visit(this);
    }

    public AbstractOperation getLeftOperand() {
        return leftOperand;
    }

    public AbstractOperation getRightOperand() {
        return rightOperand;
    }

}
