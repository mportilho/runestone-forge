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

package com.runestone.expeval.operation.datetime;

import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.OperationVisitor;

/**
 * Default behavior for date/time operations
 *
 * @author Marcelo Portilho
 */
public abstract class AbstractDateTimeOperation extends AbstractOperation {

    private final AbstractOperation leftOperand;
    private final AbstractOperation rightOperand;
    private final DateElementEnum dateElement;

    public AbstractDateTimeOperation(AbstractOperation leftOperand, AbstractOperation rightOperand, DateElementEnum dateElement) {
        super();
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.leftOperand.addParent(this);
        this.rightOperand.addParent(this);
        this.dateElement = dateElement;
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        getLeftOperand().toString(builder);
        builder.append(' ');
        builder.append(getOperationToken());
        builder.append(dateElement.toString()).append(' ');
        getRightOperand().toString(builder);
    }

    @Override
    public void accept(OperationVisitor<?> visitor) {
        getLeftOperand().accept(visitor);
        getRightOperand().accept(visitor);
        visitor.visit(this);
    }

    public final AbstractOperation getLeftOperand() {
        return leftOperand;
    }

    public final AbstractOperation getRightOperand() {
        return rightOperand;
    }

    public final DateElementEnum getDateElement() {
        return dateElement;
    }

}
