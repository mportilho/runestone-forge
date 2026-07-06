package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

final class StandardMathFunctions {

    private StandardMathFunctions() {
    }

    public static BigDecimal abs(MathContext mathContext, BigDecimal value) {
        return value.abs(mathContext);
    }

    public static BigDecimal sqrt(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.sqrt(value, mathContext);
    }

    public static BigDecimal mean(MathContext mathContext, BigDecimal[] values) {
        requireNotEmpty(values, "mean");
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            sum = sum.add(value, mathContext);
        }
        return sum.divide(BigDecimal.valueOf(values.length), mathContext);
    }

    public static BigDecimal geometricMean(MathContext mathContext, BigDecimal[] values) {
        requireNotEmpty(values, "geometric mean");
        BigDecimal product = BigDecimal.ONE;
        for (BigDecimal value : values) {
            product = product.multiply(value, mathContext);
        }
        return BigDecimalMath.root(product, BigDecimal.valueOf(values.length), mathContext);
    }

    public static BigDecimal harmonicMean(MathContext mathContext, BigDecimal[] values) {
        requireNotEmpty(values, "harmonic mean");
        BigDecimal reciprocalSum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            reciprocalSum = reciprocalSum.add(BigDecimal.ONE.divide(value, mathContext), mathContext);
        }
        return BigDecimal.valueOf(values.length).divide(reciprocalSum, mathContext);
    }

    public static BigDecimal variance(MathContext mathContext, BigDecimal[] values, int type) {
        requireNotEmpty(values, "variance");
        BigDecimal mean = mean(mathContext, values);
        BigDecimal sumSquares = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            BigDecimal difference = value.subtract(mean, mathContext);
            sumSquares = sumSquares.add(difference.multiply(difference, mathContext), mathContext);
        }
        return sumSquares.divide(BigDecimal.valueOf(values.length - type), mathContext);
    }

    public static BigDecimal stdDev(MathContext mathContext, BigDecimal[] values, int type) {
        return BigDecimalMath.sqrt(variance(mathContext, values, type), mathContext);
    }

    public static BigDecimal meanDev(MathContext mathContext, BigDecimal[] values) {
        requireNotEmpty(values, "mean deviation");
        BigDecimal mean = mean(mathContext, values);
        BigDecimal totalDeviation = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            totalDeviation = totalDeviation.add(value.subtract(mean, mathContext).abs(mathContext), mathContext);
        }
        return totalDeviation.divide(BigDecimal.valueOf(values.length), mathContext);
    }

    public static BigDecimal rule3d(MathContext mathContext, BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin2.multiply(result1, mathContext).divide(origin1, mathContext);
    }

    public static BigDecimal rule3i(MathContext mathContext, BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin1.multiply(result1, mathContext).divide(origin2, mathContext);
    }

    public static BigDecimal[] distribute(BigDecimal value, BigDecimal direction, BigDecimal[] target, BigDecimal[] limits) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(limits, "limits");
        if (target.length != limits.length) {
            throw new IllegalArgumentException("target and limits must have the same size");
        }
        BigDecimal[] distributed = java.util.Arrays.copyOf(target, target.length + 1);
        BigDecimal remainder = value;
        int start = direction.signum() >= 0 ? 0 : target.length - 1;
        int endExclusive = direction.signum() >= 0 ? target.length : -1;
        int step = direction.signum() >= 0 ? 1 : -1;
        for (int index = start; index != endExclusive && remainder.signum() != 0; index += step) {
            BigDecimal capacity = value.signum() < 0
                    ? distributed[index].subtract(limits[index])
                    : limits[index].subtract(distributed[index]);
            if (capacity.signum() <= 0) {
                continue;
            }
            BigDecimal adjustment = remainder.abs().min(capacity);
            distributed[index] = value.signum() < 0
                    ? distributed[index].subtract(adjustment)
                    : distributed[index].add(adjustment);
            remainder = value.signum() < 0 ? remainder.add(adjustment) : remainder.subtract(adjustment);
        }
        distributed[distributed.length - 1] = remainder;
        return distributed;
    }

    public static BigDecimal[] spread(MathContext mathContext, BigDecimal value, BigDecimal direction, BigDecimal[] references) {
        Objects.requireNonNull(direction, "direction");
        requireNotEmpty(references, "references");
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal reference : references) {
            total = total.add(reference, mathContext);
        }
        BigDecimal[] result = new BigDecimal[references.length];
        if (total.signum() == 0) {
            java.util.Arrays.fill(result, value.divide(BigDecimal.valueOf(references.length), mathContext));
            return result;
        }
        for (int index = 0; index < references.length; index++) {
            result[index] = value.multiply(references[index], mathContext).divide(total, mathContext);
        }
        return result;
    }

    private static void requireNotEmpty(BigDecimal[] values, String name) {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            throw new ArithmeticException(name + " of empty vector is undefined");
        }
    }
}
