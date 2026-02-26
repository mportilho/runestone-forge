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

package com.runestone.expeval.expression.calculator;

import java.util.Objects;

/**
 * Options used during {@link Calculator#calculate(java.util.List, java.util.Map, com.runestone.expeval.expression.ExpressionContext, CalculatorOptions)}.
 *
 * @param memoryMode         memory strategy used to build {@link CalculationMemory} entries. Defaults to {@link CalculatorMemoryMode#FULL}.
 * @param checkpointInterval checkpoint distance used by {@link CalculatorMemoryMode#LAZY} reconstruction. Defaults to {@value #DEFAULT_CHECKPOINT_INTERVAL}.
 * @author Marcelo Portilho
 */
public record CalculatorOptions(CalculatorMemoryMode memoryMode, int checkpointInterval) {

    public static final int DEFAULT_CHECKPOINT_INTERVAL = 64;

    private static final CalculatorOptions DEFAULT_OPTIONS = new CalculatorOptions(CalculatorMemoryMode.FULL, DEFAULT_CHECKPOINT_INTERVAL);

    public CalculatorOptions {
        Objects.requireNonNull(memoryMode, "Calculator memory mode cannot be null");
        if (checkpointInterval <= 0) {
            throw new IllegalArgumentException("Checkpoint interval must be greater than zero");
        }
    }

    public CalculatorOptions() {
        this(CalculatorMemoryMode.FULL, DEFAULT_CHECKPOINT_INTERVAL);
    }

    public CalculatorOptions(CalculatorMemoryMode memoryMode) {
        this(memoryMode, DEFAULT_CHECKPOINT_INTERVAL);
    }

    /**
     * @return default options keeping backwards-compatible behavior.
     */
    public static CalculatorOptions defaultOptions() {
        return DEFAULT_OPTIONS;
    }
}
