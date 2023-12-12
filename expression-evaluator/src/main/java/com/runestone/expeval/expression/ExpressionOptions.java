/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
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

package com.runestone.expeval.expression;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;

import java.math.MathContext;
import java.time.ZoneId;

/**
 * Options for configuring the expression evaluation.
 *
 * @param mathContext       the math context to be used for calculations
 * @param scale             the scale to be used for calculations. If null, assumes BigDecimal default behavior
 * @param conversionService the conversion service to be used for converting values
 * @param zoneId            the zone id to be used for date/time operations
 * @author Marcelo Portilho
 */
public record ExpressionOptions(MathContext mathContext, Integer scale, DataConversionService conversionService, ZoneId zoneId) {

    private static final DataConversionService DEFAULT_CONVERSION_SERVICE = new DefaultDataConversionService(false);

    /**
     * Creates a new expression options with the given math context, scale, conversion service and zone id.
     *
     * @return the expression options
     */
    public static ExpressionOptions defaultOptions() {
        return new ExpressionOptions(MathContext.DECIMAL64, null, DEFAULT_CONVERSION_SERVICE, ZoneId.systemDefault());
    }

    /**
     * Creates a new expression options with the given math context and scale.
     *
     * @param mathContext the math context to be used for calculations
     * @param scale       the scale to be used for calculations. If null, assumes BigDecimal default behavior
     * @return the expression options
     */
    public static ExpressionOptions of(MathContext mathContext, Integer scale) {
        return new ExpressionOptions(mathContext, scale, DEFAULT_CONVERSION_SERVICE, ZoneId.systemDefault());
    }

}
