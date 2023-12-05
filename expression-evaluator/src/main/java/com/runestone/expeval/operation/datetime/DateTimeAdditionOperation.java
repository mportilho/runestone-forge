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

package com.runestone.expeval.operation.datetime;

import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

public class DateTimeAdditionOperation extends AbstractDateTimeOperation {

    public DateTimeAdditionOperation(AbstractOperation leftOperand, AbstractOperation rightOperand, DateElementEnum dateElement) {
        super(leftOperand, rightOperand, dateElement);
        expectedType(ZonedDateTime.class);
    }

    @Override
    protected Object resolve(OperationContext context) {
        Temporal leftResult = getLeftOperand().evaluate(context);
        Number rightResult = getRightOperand().evaluate(context);
        return switch (getDateElement()) {
            case SECOND -> leftResult.plus(rightResult.longValue(), ChronoUnit.SECONDS);
            case MINUTE -> leftResult.plus(rightResult.longValue(), ChronoUnit.MINUTES);
            case HOUR -> leftResult.plus(rightResult.longValue(), ChronoUnit.HOURS);
            case DAY -> leftResult.plus(rightResult.longValue(), ChronoUnit.DAYS);
            case MONTH -> leftResult.plus(rightResult.longValue(), ChronoUnit.MONTHS);
            case YEAR -> leftResult.plus(rightResult.longValue(), ChronoUnit.YEARS);
        };
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        return new DateTimeAdditionOperation(getLeftOperand().copy(context), getRightOperand().copy(context), getDateElement());
    }

    @Override
    protected String getOperationToken() {
        return "plus";
    }

}
