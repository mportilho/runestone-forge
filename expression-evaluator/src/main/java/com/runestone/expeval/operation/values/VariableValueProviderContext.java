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

package com.runestone.expeval.operation.values;

import java.math.MathContext;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.function.Supplier;

/**
 * Context used to provide information to {@link VariableProvider} instances
 *
 * @param mathContext     Math context to be used by the variable provider
 * @param scale           Scale to be used by the variable provider. Can be null
 * @param zoneId          Zone id to be used by the variable provider
 * @param currentDateTime Supplier to retrieve the same current date and time used for the current evaluation
 */
public record VariableValueProviderContext(MathContext mathContext, Integer scale, ZoneId zoneId, Supplier<Temporal> currentDateTime) {

}
