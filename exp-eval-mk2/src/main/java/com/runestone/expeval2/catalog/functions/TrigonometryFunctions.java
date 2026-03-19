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
 * Contains a list of trigonometric functions. Numbers are represented as {@link BigDecimal} to provide more precision to the calculations.
 *
 * @author Marcelo Portilho
 */
public class TrigonometryFunctions {

    private static final MathContext MC = MathContext.DECIMAL128;

    /**
     * Finds the sine of a value.
     *
     * @param x The value to find the sine of.
     * @return The sine of the value.
     */
    public static BigDecimal sin(BigDecimal x) {
        return BigDecimalMath.sin(x, MC);
    }

    /**
     * Finds the cosine of a value.
     *
     * @param x The value to find the cosine of.
     * @return The cosine of the value.
     */
    public static BigDecimal cos(BigDecimal x) {
        return BigDecimalMath.cos(x, MC);
    }

    /**
     * Finds the tangent of a value.
     *
     * @param x The value to find the tangent of.
     * @return The tangent of the value.
     */
    public static BigDecimal tan(BigDecimal x) {
        return BigDecimalMath.tan(x, MC);
    }

    /**
     * Finds the arc sine of a value.
     *
     * @param x The value to find the arc sine of.
     * @return The arc sine of the value.
     */
    public static BigDecimal asin(BigDecimal x) {
        return BigDecimalMath.asin(x, MC);
    }

    /**
     * Finds the arc cosine of a value.
     *
     * @param x The value to find the arc cosine of.
     * @return The arc cosine of the value.
     */
    public static BigDecimal acos(BigDecimal x) {
        return BigDecimalMath.acos(x, MC);
    }

    /**
     * Finds the arc tangent of a value.
     *
     * @param x The value to find the arc tangent of.
     * @return The arc tangent of the value.
     */
    public static BigDecimal atan(BigDecimal x) {
        return BigDecimalMath.atan(x, MC);
    }

    /**
     * Finds the arc tangent of a value, given its x and y coordinates.
     *
     * @param y The y coordinate.
     * @param x The x coordinate.
     * @return The arc tangent of the value.
     */
    public static BigDecimal atan2(BigDecimal y, BigDecimal x) {
        return BigDecimalMath.atan2(y, x, MC);
    }

    /**
     * Finds the hyperbolic sine of a value.
     *
     * @param x The value to find the hyperbolic sine of.
     * @return The hyperbolic sine of the value.
     */
    public static BigDecimal sinh(BigDecimal x) {
        return BigDecimalMath.sinh(x, MC);
    }

    /**
     * Finds the hyperbolic cosine of a value.
     *
     * @param x The value to find the hyperbolic cosine of.
     * @return The hyperbolic cosine of the value.
     */
    public static BigDecimal cosh(BigDecimal x) {
        return BigDecimalMath.cosh(x, MC);
    }

    /**
     * Finds the hyperbolic tangent of a value.
     *
     * @param x The value to find the hyperbolic tangent of.
     * @return The hyperbolic tangent of the value.
     */
    public static BigDecimal tanh(BigDecimal x) {
        return BigDecimalMath.tanh(x, MC);
    }

    /**
     * Finds the hyperbolic arc sine of a value.
     *
     * @param x The value to find the hyperbolic arc sine of.
     * @return The hyperbolic arc sine of the value.
     */
    public static BigDecimal asinh(BigDecimal x) {
        return BigDecimalMath.asinh(x, MC);
    }

    /**
     * Finds the hyperbolic arc cosine of a value.
     *
     * @param x The value to find the hyperbolic arc cosine of.
     * @return The hyperbolic arc cosine of the value.
     */
    public static BigDecimal acosh(BigDecimal x) {
        return BigDecimalMath.acosh(x, MC);
    }

    /**
     * Finds the hyperbolic arc tangent of a value.
     *
     * @param x The value to find the hyperbolic arc tangent of.
     * @return The hyperbolic arc tangent of the value.
     */
    public static BigDecimal atanh(BigDecimal x) {
        return BigDecimalMath.atanh(x, MC);
    }

}
