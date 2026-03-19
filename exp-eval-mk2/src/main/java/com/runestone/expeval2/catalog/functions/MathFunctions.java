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
import java.util.Arrays;
import java.util.Objects;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_EVEN;

/**
 * Contains a list of mathematical functions. Numbers are represented as {@link BigDecimal} to provide more precision to the calculations.
 *
 * @author Marcelo Portilho
 */
public class MathFunctions {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final int KAHAN_THRESHOLD = 1000;

    /**
     * Finds the mean value of a list of values. The mean value is the sum of all values divided by the number of values.
     *
     * @param p Array of values
     * @return mean value
     */
    public static BigDecimal mean(BigDecimal[] p) {
        int n = p.length;
        if (n == 0) {
            throw new ArithmeticException("Empty array");
        }
        BigDecimal size = valueOf(n);
        if (n == 1) {
            return p[0];
        } else if (n == 2) {
            return p[0].add(p[1]).divide(size, MC);
        }

        if (n >= KAHAN_THRESHOLD) {
            return valueOf(KahanSummation.sum(p)).divide(size, MC);
        }

        BigDecimal sum = ZERO;
        for (BigDecimal param : p) {
            sum = sum.add(param);
        }
        return sum.divide(size, MC);
    }

    /**
     * Finds the geometric mean value of a list of values. The geometric mean value is the nth root of the product of all values.
     *
     * @param p Array of values
     * @return geometric mean value
     */
    public static BigDecimal geometricMean(BigDecimal[] p) {
        int n = p.length;
        if (n == 0) {
            throw new ArithmeticException("Empty array");
        }
        BigDecimal size = valueOf(n);
        BigDecimal x = ONE;
        for (BigDecimal param : p) {
            x = x.multiply(param, MC);
        }
        return BigDecimalMath.root(x, size, MC);
    }

    /**
     * Finds the harmonic mean value of a list of values. The harmonic mean value is the reciprocal of the arithmetic mean of the reciprocals of the values.
     *
     * @param p Array of values
     * @return harmonic mean value
     */
    public static BigDecimal harmonicMean(BigDecimal[] p) {
        int n = p.length;
        BigDecimal size = valueOf(n);
        BigDecimal x = ZERO;
        for (BigDecimal param : p) {
            x = x.add(ONE.divide(param, MC), MC);
        }
        return size.divide(x, MC);
    }

    /**
     * Finds the median value of a list of values. The median value is the middle value of a sorted list of values.
     *
     * @param p    Array of values
     * @param type Value 0: Population Standard Deviation; Value 1: Sample Standard Deviation
     * @return variance
     */
    public static BigDecimal variance(BigDecimal[] p, int type) {
        int n = p.length;
        if (n == 0) {
            throw new ArithmeticException("Empty array");
        }
        BigDecimal size = valueOf(n);
        BigDecimal nMinusType = valueOf(n - type);

        if (n >= KAHAN_THRESHOLD) {
            double sum = 0.0;
            double sumSquares = 0.0;
            double cSum = 0.0;
            double cSumSquares = 0.0;

            for (BigDecimal val : p) {
                double x = val.doubleValue();
                // Kahan for sum
                double ySum = x - cSum;
                double tSum = sum + ySum;
                cSum = (tSum - sum) - ySum;
                sum = tSum;

                // Kahan for sumSquares
                double xSq = x * x;
                double ySq = xSq - cSumSquares;
                double tSq = sumSquares + ySq;
                cSumSquares = (tSq - sumSquares) - ySq;
                sumSquares = tSq;
            }

            BigDecimal bdSum = valueOf(sum);
            BigDecimal bdSumSquares = valueOf(sumSquares);
            BigDecimal mean = bdSum.divide(size, MC);
            BigDecimal numerator = bdSumSquares.subtract(bdSum.multiply(mean, MC), MC);
            return numerator.divide(nMinusType, MC);
        }

        BigDecimal sum = ZERO;
        BigDecimal sumSquares = ZERO;
        for (BigDecimal param : p) {
            sum = sum.add(param);
            sumSquares = sumSquares.add(param.multiply(param, MC), MC);
        }
        BigDecimal mean = sum.divide(size, MC);
        BigDecimal numerator = sumSquares.subtract(sum.multiply(mean, MC), MC);
        return numerator.divide(nMinusType, MC);
    }

    /**
     * Finds the standard deviation value of a list of values. The standard deviation value is the square root of the variance.
     *
     * @param p    Array of values
     * @param type Value 0: Population Standard Deviation; Value 1: Sample Standard Deviation
     * @return standard deviation
     */
    public static BigDecimal stdDev(BigDecimal[] p, int type) {
        return BigDecimalMath.sqrt(variance(p, type), MC);
    }

    /**
     * Finds the mean deviation value of a list of values. The mean deviation value is the mean of the absolute deviations of a set of
     * data about the data's mean.
     *
     * @param p Array of values
     * @return mean deviation
     */
    public static BigDecimal meanDev(BigDecimal[] p) {
        int n = p.length;
        if (n == 0) {
            throw new ArithmeticException("Empty array");
        }
        BigDecimal size = valueOf(n);

        if (n >= KAHAN_THRESHOLD) {
            double mean = KahanSummation.sum(p) / n;
            return valueOf(KahanSummation.sumAbsoluteDeviations(p, mean)).divide(size, MC);
        }

        BigDecimal sum = ZERO;
        for (BigDecimal param : p) {
            sum = sum.add(param);
        }
        BigDecimal mean = sum.divide(size, MC);
        BigDecimal x = ZERO;
        for (BigDecimal param : p) {
            x = x.add(param.subtract(mean, MC).abs(MC), MC);
        }
        return x.divide(size, MC);
    }

    /**
     * Finds the natural logarithm value of a value.
     *
     * @param value Value
     * @return natural logarithm value
     */
    public static BigDecimal ln(BigDecimal value) {
        return BigDecimalMath.log(value, MC);
    }

    /**
     * Finds the binary logarithm value of a value.
     *
     * @param value Value
     * @return binary logarithm value
     */
    public static BigDecimal lb(BigDecimal value) {
        return BigDecimalMath.log2(value, MC);
    }

    /**
     * Finds the logarithm value of a value with a specific base.
     *
     * @param base  Base
     * @param value Value
     * @return logarithm value
     */
    public static BigDecimal log(BigDecimal base, BigDecimal value) {
        return BigDecimalMath.log(value, MC).divide(BigDecimalMath.log(base, MC), MC);
    }

    /**
     * Finds the direct rule of three value of a list of values. The direct rule of three value is the result of a proportionality
     * between two values.
     *
     * @param origin1 First origin value
     * @param result1 First result value
     * @param origin2 Second origin value
     * @return direct rule of three value
     */
    public static BigDecimal rule3d(BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin2.multiply(result1, MC).divide(origin1, MC);
    }

    /**
     * Finds the inverse rule of three value of a list of values. The inverse rule of three value is the result of a proportionality
     * between two values.
     *
     * @param origin1 First origin value
     * @param result1 First result value
     * @param origin2 Second origin value
     * @return inverse rule of three value
     */
    public static BigDecimal rule3i(BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin1.multiply(result1, MC).divide(origin2, MC);
    }

    /**
     * Distributes sequentially a value through an array of numbers. If the value is positive, it's distributed adding on each array's position
     * until an upper limit. If the value is negative, it's distributed subtracting on each array's position until a lower limit.
     *
     * @param value     Value to be distributed
     * @param direction Direction of the distribution. Positive (or zero) values distribute from the first position to the last. Negative values distribute from the last position to the first.
     * @param target    Array of numbers to be adjusted by the value
     * @param limits    Array of limits. If the value is positive, it's added to each position until the limit. If the value is negative, it's subtracted from each position until the limit.
     * @return An array containing the adjusted values and the remainder of the distribution in the last position
     */
    public static BigDecimal[] distribute(BigDecimal value, BigDecimal direction, BigDecimal[] target, BigDecimal[] limits) {
        Objects.requireNonNull(value, "Value cannot be null");
        Objects.requireNonNull(direction, "Direction cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");
        Objects.requireNonNull(limits, "Limits cannot be null");

        BigDecimal[] distributed = new BigDecimal[target.length + 1];
        System.arraycopy(target, 0, distributed, 0, target.length);
        BigDecimal currValue = value;

        if (currValue.compareTo(ZERO) >= 0) {
            if (direction.compareTo(ZERO) >= 0) {
                for (int i = 0; i < target.length; i++) {
                    currValue = positiveDistribution(currValue, target[i], limits[i], i, distributed);
                    if (currValue.compareTo(ZERO) == 0) break;
                }
            } else {
                for (int i = target.length - 1; i >= 0; i--) {
                    currValue = positiveDistribution(currValue, target[i], limits[i], i, distributed);
                    if (currValue.compareTo(ZERO) == 0) break;
                }
            }
        } else {
            currValue = currValue.abs();
            if (direction.compareTo(ZERO) >= 0) {
                for (int i = 0; i < target.length; i++) {
                    currValue = negativeDistribution(currValue, target[i], limits[i], i, distributed);
                    if (currValue.compareTo(ZERO) == 0) break;
                }
            } else {
                for (int i = target.length - 1; i >= 0; i--) {
                    currValue = negativeDistribution(currValue, target[i], limits[i], i, distributed);
                    if (currValue.compareTo(ZERO) == 0) break;
                }
            }
            currValue = currValue.negate();
        }
        distributed[distributed.length - 1] = currValue;
        return distributed;
    }

    private static BigDecimal positiveDistribution(BigDecimal value, BigDecimal target, BigDecimal limit, int i, BigDecimal[] distributed) {
        if (target.compareTo(limit) >= 0) {
            distributed[i] = target;
            return value;
        }
        BigDecimal capacity = limit.subtract(target);
        if (value.compareTo(capacity) <= 0) {
            distributed[i] = target.add(value);
            return ZERO.setScale(value.scale(), HALF_EVEN);
        } else {
            distributed[i] = limit;
            return value.subtract(capacity);
        }
    }

    private static BigDecimal negativeDistribution(BigDecimal value, BigDecimal target, BigDecimal limit, int i, BigDecimal[] distributed) {
        if (target.compareTo(limit) <= 0) {
            distributed[i] = target;
            return value;
        }
        BigDecimal capacity = target.subtract(limit);
        if (value.compareTo(capacity) <= 0) {
            distributed[i] = target.subtract(value);
            return ZERO.setScale(value.scale(), HALF_EVEN);
        } else {
            distributed[i] = limit;
            return value.subtract(capacity);
        }
    }

    /**
     * Spreads a value through an array of numbers. The value is distributed proportionally to the reference values and the results are
     * rounded to the same scale of the value.
     * <p>
     * If the sum of references is zero, the value is distributed equally among all positions.
     * If the value is negative, the resulting parts will maintain the proportional sign of the references.
     *
     * @param value      Value to be distributed (can be positive or negative)
     * @param direction  Direction of the distribution of the remaining value of decimal divisions. Positive (or zero) values distribute from the first position to the last. Negative values distribute from the last position to the first.
     * @param references Array of reference values
     * @return An array containing the adjusted values
     */
    public static BigDecimal[] spread(BigDecimal value, BigDecimal direction, BigDecimal[] references) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (references == null || references.length == 0) {
            throw new IllegalArgumentException("References cannot be null or empty");
        }

        int scale = value.scale();
        if (value.compareTo(ZERO) == 0) {
            BigDecimal[] distributed = new BigDecimal[references.length];
            Arrays.fill(distributed, ZERO.setScale(scale, HALF_EVEN));
            return distributed;
        }

        BigDecimal totalSum = ZERO;
        for (BigDecimal ref : references) {
            totalSum = totalSum.add(ref);
        }

        BigDecimal[] distributed = new BigDecimal[references.length];
        BigDecimal distributedSum = ZERO;
        if (totalSum.compareTo(ZERO) != 0) {
            BigDecimal factor = value.divide(totalSum, MC);
            for (int i = 0; i < references.length; i++) {
                BigDecimal distValue = references[i].multiply(factor).setScale(scale, HALF_EVEN);
                distributed[i] = distValue;
                distributedSum = distributedSum.add(distValue);
            }
        } else {
            BigDecimal size = valueOf(references.length);
            BigDecimal val = value.divide(size, HALF_EVEN).setScale(scale, HALF_EVEN);
            distributedSum = val.multiply(size);
            Arrays.fill(distributed, val);
        }

        BigDecimal difference = value.subtract(distributedSum).setScale(scale, HALF_EVEN);
        if (difference.compareTo(ZERO) != 0) {
            int indexToAdjust = -1;
            if (direction.compareTo(ZERO) >= 0) {
                for (int i = 0; i < references.length; i++) {
                    if (references[i].compareTo(ZERO) != 0) {
                        indexToAdjust = i;
                        break;
                    }
                }
                if (indexToAdjust == -1) indexToAdjust = 0;
            } else {
                for (int i = distributed.length - 1; i >= 0; i--) {
                    if (references[i].compareTo(ZERO) != 0) {
                        indexToAdjust = i;
                        break;
                    }
                }
                if (indexToAdjust == -1) indexToAdjust = distributed.length - 1;
            }
            distributed[indexToAdjust] = distributed[indexToAdjust].add(difference);
        }
        return distributed;
    }

    /**
     * Utility class for Kahan summation algorithm to reduce floating-point errors
     * while performing fast summation of BigDecimal values converted to double.
     */
    private static class KahanSummation {

        /**
         * Sums an array of BigDecimal values using the Kahan summation algorithm.
         *
         * @param values Array of values to sum
         * @return Sum as a double
         */
        static double sum(BigDecimal[] values) {
            double sum = 0.0;
            double c = 0.0;
            for (BigDecimal val : values) {
                double x = val.doubleValue();
                double y = x - c;
                double t = sum + y;
                c = (t - sum) - y;
                sum = t;
            }
            return sum;
        }

        /**
         * Sums an array of BigDecimal values squared using the Kahan summation algorithm.
         *
         * @param values Array of values to sum their squares
         * @return Sum of squares as a double
         */
        static double sumSquares(BigDecimal[] values) {
            double sum = 0.0;
            double c = 0.0;
            for (BigDecimal val : values) {
                double x = val.doubleValue();
                double xSquared = x * x;
                double y = xSquared - c;
                double t = sum + y;
                c = (t - sum) - y;
                sum = t;
            }
            return sum;
        }

        /**
         * Sums absolute deviations from a mean using the Kahan summation algorithm.
         *
         * @param values Array of values
         * @param mean   The mean to subtract
         * @return Sum of absolute deviations as a double
         */
        static double sumAbsoluteDeviations(BigDecimal[] values, double mean) {
            double sum = 0.0;
            double c = 0.0;
            for (BigDecimal val : values) {
                double x = Math.abs(val.doubleValue() - mean);
                double y = x - c;
                double t = sum + y;
                c = (t - sum) - y;
                sum = t;
            }
            return sum;
        }
    }


}
