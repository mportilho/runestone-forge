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

package com.runestone.expeval2.catalog.functions;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Contains a list of logarithm functions.
 *
 * @author Marcelo Portilho
 */
public class LogarithmFunctions {

    private static final double LN_2 = Math.log(2);

    /**
     * Finds the natural logarithm value of a value.
     *
     * @param mc    MathContext for precision control
     * @param value Value
     * @return natural logarithm value
     */
    public static BigDecimal ln(MathContext mc, BigDecimal value) {
        return BigDecimalMath.log(value, mc);
    }

    /**
     * Finds the binary logarithm value of a value.
     *
     * @param mc    MathContext for precision control
     * @param value Value
     * @return binary logarithm value
     */
    public static BigDecimal lb(MathContext mc, BigDecimal value) {
        return BigDecimalMath.log2(value, mc);
    }

    /**
     * Finds the logarithm value of a value with a specific base.
     *
     * @param mc    MathContext for precision control
     * @param base  Base
     * @param value Value
     * @return logarithm value
     */
    public static BigDecimal log(MathContext mc, BigDecimal base, BigDecimal value) {
        return BigDecimalMath.log(value, mc).divide(BigDecimalMath.log(base, mc), mc);
    }

    /**
     * Finds the natural logarithm value of a value using double precision arithmetic.
     * Faster than {@link #ln(MathContext, BigDecimal)} but with reduced precision.
     *
     * @param value Value
     * @return natural logarithm value, {@link Double#NEGATIVE_INFINITY} if value is zero,
     * or {@link Double#NaN} if value is negative
     */
    public static double lnFast(double value) {
        return Math.log(value);
    }

    /**
     * Finds the binary logarithm value of a value using double precision arithmetic.
     * Faster than {@link #lb(MathContext, BigDecimal)} but with reduced precision.
     *
     * @param value Value
     * @return binary logarithm value, {@link Double#NEGATIVE_INFINITY} if value is zero,
     * or {@link Double#NaN} if value is negative
     */
    public static double lbFast(double value) {
        return Math.log(value) / LN_2;
    }

    /**
     * Finds the logarithm value of a value with a specific base using double precision arithmetic.
     * Faster than {@link #log(MathContext, BigDecimal, BigDecimal)} but with reduced precision.
     *
     * @param base  Base of the logarithm
     * @param value Value
     * @return logarithm value, {@link Double#NEGATIVE_INFINITY} if value is zero,
     * or {@link Double#NaN} if value or base is negative, or if base is 1
     */
    public static double logFast(double base, double value) {
        return Math.log(value) / Math.log(base);
    }

}
