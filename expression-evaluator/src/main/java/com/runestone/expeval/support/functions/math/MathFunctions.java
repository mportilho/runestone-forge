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

package com.runestone.expeval.support.functions.math;

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

    /**
     * Finds the mean value of a list of values. The mean value is the sum of all values divided by the number of values.
     *
     * @param p  Array of values
     * @param mc MathContext
     * @return mean value
     */
    public static BigDecimal mean(BigDecimal[] p, MathContext mc) {
        if (p.length == 1) {
            return p[0];
        } else if (p.length == 2) {
            return p[0].add(p[1]).divide(valueOf(2), mc);
        }
        BigDecimal sum = ZERO;
        for (BigDecimal param : p) {
            sum = sum.add(param);
        }
        return sum.divide(BigDecimal.valueOf(p.length), mc);
    }

    /**
     * Finds the geometric mean value of a list of values. The geometric mean value is the nth root of the product of all values.
     *
     * @param p  Array of values
     * @param mc MathContext
     * @return geometric mean value
     */
    public static BigDecimal geometricMean(BigDecimal[] p, MathContext mc) {
        BigDecimal x = ONE;
        for (BigDecimal param : p) {
            x = x.multiply(param, mc);
        }
        return BigDecimalMath.root(x, valueOf(p.length), mc);
    }

    /**
     * Finds the harmonic mean value of a list of values. The harmonic mean value is the reciprocal of the arithmetic mean of the reciprocals of the values.
     *
     * @param p  Array of values
     * @param mc MathContext
     * @return harmonic mean value
     */
    public static BigDecimal harmonicMean(BigDecimal[] p, MathContext mc) {
        BigDecimal x = ZERO;
        for (BigDecimal param : p) {
            x = x.add(ONE.divide(param, mc), mc);
        }
        return valueOf(p.length).divide(x, mc);
    }

    /**
     * Finds the median value of a list of values. The median value is the middle value of a sorted list of values.
     *
     * @param p    Array of values
     * @param type Value 0: Population Standard Deviation; Value 1: Sample Standard Deviation
     * @param mc   MathContext
     * @return variance
     */
    public static BigDecimal variance(BigDecimal[] p, int type, MathContext mc) {
        BigDecimal mean = mean(p, mc);
        BigDecimal x = ZERO;
        for (BigDecimal param : p) {
            x = x.add(BigDecimalMath.pow(param.subtract(mean, mc), 2, mc), mc);
        }
        return ONE.divide(valueOf(p.length - type), mc).multiply(x, mc);
    }

    /**
     * Finds the standard deviation value of a list of values. The standard deviation value is the square root of the variance.
     *
     * @param p    Array of values
     * @param type Value 0: Population Standard Deviation; Value 1: Sample Standard Deviation
     * @param mc   MathContext
     * @return standard deviation
     */
    public static BigDecimal stdDev(BigDecimal[] p, int type, MathContext mc) {
        return BigDecimalMath.sqrt(variance(p, type, mc), mc);
    }

    /**
     * Finds the mean deviation value of a list of values. The mean deviation value is the mean of the absolute deviations of a set of
     * data about the data's mean.
     *
     * @param p  Array of values
     * @param mc MathContext
     * @return mean deviation
     */
    public static BigDecimal meanDev(BigDecimal[] p, MathContext mc) {
        BigDecimal mean = mean(p, mc);
        BigDecimal x = ZERO;
        for (BigDecimal param : p) {
            x = x.add(param.subtract(mean, mc).abs(mc), mc);
        }
        return ONE.divide(valueOf(p.length), mc).multiply(x, mc);
    }

    /**
     * Finds the direct rule of three value of a list of values. The direct rule of three value is the result of a proportionality
     * between two values.
     *
     * @param origin1 First origin value
     * @param result1 First result value
     * @param origin2 Second origin value
     * @param mc      MathContext
     * @return direct rule of three value
     */
    public static BigDecimal rule3d(BigDecimal origin1, BigDecimal result1, BigDecimal origin2, MathContext mc) {
        return origin2.multiply(result1, mc).divide(origin1, mc);
    }

    /**
     * Finds the inverse rule of three value of a list of values. The inverse rule of three value is the result of a proportionality
     * between two values.
     *
     * @param origin1 First origin value
     * @param result1 First result value
     * @param origin2 Second origin value
     * @param mc      MathContext
     * @return inverse rule of three value
     */
    public static BigDecimal rule3i(BigDecimal origin1, BigDecimal result1, BigDecimal origin2, MathContext mc) {
        return origin1.multiply(origin2, mc).divide(result1, mc);
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
        BigDecimal newValue = target.add(value);
        if (newValue.compareTo(limit) > 0) {
            distributed[i] = limit;
            value = newValue.subtract(limit);
        } else {
            distributed[i] = newValue;
            value = ZERO.setScale(target.scale(), HALF_EVEN);
        }
        return value;
    }

    private static BigDecimal negativeDistribution(BigDecimal value, BigDecimal target, BigDecimal limit, int i, BigDecimal[] distributed) {
        BigDecimal newValue = target.subtract(value);
        if (newValue.compareTo(limit) < 0) {
            distributed[i] = limit;
            value = value.subtract(target.subtract(limit));
        } else {
            distributed[i] = newValue;
            value = ZERO.setScale(target.scale(), HALF_EVEN);
        }
        return value;
    }

    /**
     * Spreads a value through an array of numbers. The value is distributed proportionally to the reference values and the results are
     * rounded to the same scale of the value.
     *
     * @param value      Value to be distributed
     * @param direction  Direction of the distribution of the remaining value of decimal divisions. Positive (or zero) values distribute from the first position to the last. Negative values distribute from the last position to the first.
     * @param references Array of reference values
     * @return An array containing the adjusted values and the remainder of the distribution in the last position
     */
    public static BigDecimal[] spread(BigDecimal value, BigDecimal direction, BigDecimal[] references) {
        if (value == null || value.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Value cannot be null or negative");
        }
        if (references == null || references.length == 0) {
            throw new IllegalArgumentException("References cannot be null or empty");
        }

        if (value.compareTo(ZERO) == 0) {
            BigDecimal[] distributed = new BigDecimal[references.length];
            Arrays.fill(distributed, ZERO.setScale(value.scale(), HALF_EVEN));
            return distributed;
        }

        BigDecimal totalSum = ZERO;
        for (BigDecimal ref : references) {
            totalSum = totalSum.add(ref);
        }

        BigDecimal[] distributed = new BigDecimal[references.length];
        BigDecimal distributedSum = ZERO;
        if (totalSum.compareTo(ZERO) != 0) {
            for (int i = 0; i < references.length; i++) {
                BigDecimal coefficient = references[i].divide(totalSum, 16, HALF_EVEN);
                BigDecimal distValue = value.multiply(coefficient).setScale(value.scale(), HALF_EVEN);
                distributed[i] = distValue;
                distributedSum = distributedSum.add(distValue);
            }
        } else {
            BigDecimal val = value.divide(BigDecimal.valueOf(references.length), HALF_EVEN).setScale(value.scale(), HALF_EVEN);
            distributedSum = val.multiply(BigDecimal.valueOf(references.length));
            Arrays.fill(distributed, val);
        }

        BigDecimal difference = value.subtract(distributedSum).setScale(value.scale(), HALF_EVEN);
        if (difference.compareTo(ZERO) != 0) {
            if (direction.compareTo(ZERO) >= 0) {
                for (int i = 0; i < distributed.length; i++) {
                    if (distributed[i].compareTo(ZERO) > 0) {
                        distributed[i] = distributed[i].add(difference);
                        break;
                    }
                }
            } else {
                for (int i = distributed.length - 1; i >= 0; i--) {
                    if (distributed[i].compareTo(ZERO) > 0) {
                        distributed[i] = distributed[i].add(difference);
                        break;
                    }
                }
            }
        }
        return distributed;
    }

}
