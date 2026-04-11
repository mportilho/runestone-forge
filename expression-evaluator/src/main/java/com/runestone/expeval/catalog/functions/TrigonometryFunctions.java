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

package com.runestone.expeval.catalog.functions;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Contains a list of trigonometric functions. Numbers are represented as {@link BigDecimal} to provide more precision to the calculations.
 *
 * @author Marcelo Portilho
 */
public class TrigonometryFunctions {

    public static BigDecimal sin(MathContext mc, BigDecimal x) {
        return BigDecimalMath.sin(x, mc);
    }

    public static BigDecimal cos(MathContext mc, BigDecimal x) {
        return BigDecimalMath.cos(x, mc);
    }

    public static BigDecimal tan(MathContext mc, BigDecimal x) {
        return BigDecimalMath.tan(x, mc);
    }

    public static BigDecimal asin(MathContext mc, BigDecimal x) {
        return BigDecimalMath.asin(x, mc);
    }

    public static BigDecimal acos(MathContext mc, BigDecimal x) {
        return BigDecimalMath.acos(x, mc);
    }

    public static BigDecimal atan(MathContext mc, BigDecimal x) {
        return BigDecimalMath.atan(x, mc);
    }

    public static BigDecimal atan2(MathContext mc, BigDecimal y, BigDecimal x) {
        return BigDecimalMath.atan2(y, x, mc);
    }

    public static BigDecimal sinh(MathContext mc, BigDecimal x) {
        return BigDecimalMath.sinh(x, mc);
    }

    public static BigDecimal cosh(MathContext mc, BigDecimal x) {
        return BigDecimalMath.cosh(x, mc);
    }

    public static BigDecimal tanh(MathContext mc, BigDecimal x) {
        return BigDecimalMath.tanh(x, mc);
    }

    public static BigDecimal asinh(MathContext mc, BigDecimal x) {
        return BigDecimalMath.asinh(x, mc);
    }

    public static BigDecimal acosh(MathContext mc, BigDecimal x) {
        return BigDecimalMath.acosh(x, mc);
    }

    public static BigDecimal atanh(MathContext mc, BigDecimal x) {
        return BigDecimalMath.atanh(x, mc);
    }
}
