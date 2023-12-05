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

package com.runestone.expeval.support.callsite;

import java.math.MathContext;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.function.Supplier;

/**
 * Contextual information for the current function call.
 *
 * @param mathContext     the math context to be used for calculations
 * @param scale           the scale to be used for calculations. Can be null
 * @param zoneId          the zone id to be used for date/time operations
 * @param currentDateTime Supplier to retrieve the same current date and time used for the current evaluation
 */
public record CallSiteContext(MathContext mathContext, Integer scale, ZoneId zoneId, Supplier<Temporal> currentDateTime) {

    public CallSiteContext {
        if (mathContext == null) {
            throw new IllegalArgumentException("MathContext cannot be null");
        }
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId cannot be null");
        }
        if (currentDateTime == null) {
            throw new IllegalArgumentException("Current date/time supplier cannot be null");
        }
    }
}
