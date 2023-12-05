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

package com.runestone.expeval.operation.values.constant;

import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.values.AbstractConstantValueOperation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateTimeConstantValueOperation extends AbstractConstantValueOperation {

    private final String offset;

    public DateTimeConstantValueOperation(String value, String offset) {
        super(value);
        this.offset = offset;
        expectedType(ZonedDateTime.class);
    }

    @Override
    protected AbstractOperation createClone(CloningContext context) {
        return new DateTimeConstantValueOperation(getValue(), offset);
    }

    @Override
    protected Object resolve(OperationContext context) {
        ZoneId zoneId = offset != null && !offset.isBlank() ? ZoneOffset.of(offset) : context.zoneId();
        return ZonedDateTime.of(LocalDateTime.parse(getValue()), zoneId);
    }

    @Override
    protected void formatRepresentation(StringBuilder builder) {
        if (getCache() != null) {
            builder.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((TemporalAccessor) getCache()));
        } else {
            builder.append(getValue());
        }
    }
}
