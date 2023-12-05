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

package com.runestone.expeval.operation.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval.operation.AbstractBinaryOperation;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;

import java.math.BigDecimal;

public class RootOperation extends AbstractBinaryOperation {

	public RootOperation(AbstractOperation leftOperand, AbstractOperation rightOperand) {
		super(leftOperand, rightOperand);
		expectedType(BigDecimal.class);
	}

	@Override
	protected Object resolve(OperationContext context) {
		BigDecimal leftValue = getLeftOperand().evaluate(context);
		BigDecimal rightValue = getRightOperand().evaluate(context);
		return BigDecimalMath.root(leftValue, rightValue, context.mathContext());
	}

	@Override
	public void formatRepresentation(StringBuilder builder) {
		getRightOperand().toString(builder);
		builder.append(getOperationToken()).append('(');
		getLeftOperand().toString(builder);
		builder.append(')');
	}

	@Override
	protected AbstractOperation createClone(CloningContext context) {
		return new RootOperation(getLeftOperand().copy(context), getRightOperand().copy(context));
	}

	@Override
	protected String getOperationToken() {
		return "\u221A";
	}

}
