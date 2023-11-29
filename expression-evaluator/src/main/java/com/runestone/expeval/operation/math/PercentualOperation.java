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

package com.runestone.expeval.operation.math;

import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.AbstractUnaryOperator;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;

import java.math.BigDecimal;

public class PercentualOperation extends AbstractUnaryOperator {

	private static final BigDecimal PERCENT = new BigDecimal("0.01");

	public PercentualOperation(AbstractOperation operand) {
		super(operand, OperatorPosition.RIGHT);
		expectedType(BigDecimal.class);
	}

	@Override
	protected Object resolve(OperationContext context) {
		return getOperand().<BigDecimal>evaluate(context).multiply(PERCENT, context.mathContext());
	}

	@Override
	protected AbstractOperation createClone(CloningContext context) {
		return new PercentualOperation(getOperand().copy(context));
	}

	@Override
	protected String getOperationToken() {
		return "%";
	}

}
