package com.runestone.expeval2.catalog.functions;

import java.math.BigDecimal;

/**
 * Utility class for Kahan summation algorithm to reduce floating-point errors
 * while performing fast summation of BigDecimal values converted to double.
 */
class KahanSummation {

    /**
     * Sums an array of BigDecimal values using the Kahan summation algorithm.
     *
     * @param values Array of values to sum
     * @return Sum as a double
     */
    public static double sum(BigDecimal[] values) {
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
    public static double sumSquares(BigDecimal[] values) {
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
    public static double sumAbsoluteDeviations(BigDecimal[] values, double mean) {
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
