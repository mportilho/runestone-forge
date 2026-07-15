package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;

final class MathBuiltInFunctions {

    private static final int KAHAN_THRESHOLD = 1000;

    private final MathContext mathContext;

    MathBuiltInFunctions(MathContext mathContext) {
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
    }

    static List<FunctionDescriptor> descriptors(MathContext mathContext) {
        return ReflectedFunctionImporter
                .importAll(
                        new MathBuiltInFunctions(mathContext),
                        FunctionPurity.FOLDABLE)
                .toList();
    }

    public BigDecimal abs(BigDecimal value) {
        return value.abs();
    }

    public BigDecimal sqrt(BigDecimal value) {
        return BigDecimalMath.sqrt(value, mathContext);
    }

    public BigDecimal mean(List<BigDecimal> values) {
        BigDecimal[] numbers = BuiltInFunctionSupport.numbers(values);
        int size = numbers.length;
        if (size == 0) {
            throw new ArithmeticException("mean of empty vector is undefined");
        }
        if (size == 1) {
            return numbers[0];
        }
        if (size == 2) {
            return numbers[0].add(numbers[1]).divide(BigDecimal.valueOf(size), mathContext);
        }
        if (size >= KAHAN_THRESHOLD) {
            return BigDecimal.valueOf(BuiltInFunctionSupport.kahanSum(numbers)).divide(BigDecimal.valueOf(size), mathContext);
        }
        BigDecimal sum = ZERO;
        for (BigDecimal number : numbers) {
            sum = sum.add(number);
        }
        return sum.divide(BigDecimal.valueOf(size), mathContext);
    }

    public BigDecimal geometricMean(List<BigDecimal> values) {
        BigDecimal[] numbers = BuiltInFunctionSupport.numbers(values);
        if (numbers.length == 0) {
            throw new ArithmeticException("geometric mean of empty vector is undefined");
        }
        BigDecimal product = ONE;
        for (BigDecimal number : numbers) {
            product = product.multiply(number, mathContext);
        }
        return BigDecimalMath.root(product, BigDecimal.valueOf(numbers.length), mathContext);
    }

    public BigDecimal harmonicMean(List<BigDecimal> values) {
        BigDecimal[] numbers = BuiltInFunctionSupport.numbers(values);
        BigDecimal reciprocalSum = ZERO;
        for (BigDecimal number : numbers) {
            reciprocalSum = reciprocalSum.add(ONE.divide(number, mathContext), mathContext);
        }
        return BigDecimal.valueOf(numbers.length).divide(reciprocalSum, mathContext);
    }

    public BigDecimal variance(List<BigDecimal> values, BigDecimal type) {
        BigDecimal[] numbers = BuiltInFunctionSupport.numbers(values);
        int size = numbers.length;
        if (size == 0) {
            throw new ArithmeticException("variance of empty vector is undefined");
        }
        BigDecimal divisor = BigDecimal.valueOf(size - BuiltInFunctionSupport.integer(type));
        if (size >= KAHAN_THRESHOLD) {
            double sum = 0.0;
            double sumSquares = 0.0;
            double sumCompensation = 0.0;
            double squareCompensation = 0.0;
            for (BigDecimal number : numbers) {
                double value = number.doubleValue();
                double correctedSum = value - sumCompensation;
                double nextSum = sum + correctedSum;
                sumCompensation = (nextSum - sum) - correctedSum;
                sum = nextSum;

                double correctedSquare = value * value - squareCompensation;
                double nextSquare = sumSquares + correctedSquare;
                squareCompensation = (nextSquare - sumSquares) - correctedSquare;
                sumSquares = nextSquare;
            }
            BigDecimal decimalSum = BigDecimal.valueOf(sum);
            BigDecimal mean = decimalSum.divide(BigDecimal.valueOf(size), mathContext);
            BigDecimal numerator = BigDecimal.valueOf(sumSquares).subtract(decimalSum.multiply(mean, mathContext), mathContext);
            return numerator.divide(divisor, mathContext);
        }
        BigDecimal sum = ZERO;
        BigDecimal sumSquares = ZERO;
        for (BigDecimal number : numbers) {
            sum = sum.add(number);
            sumSquares = sumSquares.add(number.multiply(number, mathContext), mathContext);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(size), mathContext);
        BigDecimal numerator = sumSquares.subtract(sum.multiply(mean, mathContext), mathContext);
        return numerator.divide(divisor, mathContext);
    }

    public BigDecimal stdDev(List<BigDecimal> values, BigDecimal type) {
        return BigDecimalMath.sqrt(variance(values, type), mathContext);
    }

    public BigDecimal meanDev(List<BigDecimal> values) {
        BigDecimal[] numbers = BuiltInFunctionSupport.numbers(values);
        int size = numbers.length;
        if (size == 0) {
            throw new ArithmeticException("mean deviation of empty vector is undefined");
        }
        if (size >= KAHAN_THRESHOLD) {
            double mean = BuiltInFunctionSupport.kahanSum(numbers) / size;
            return BigDecimal.valueOf(BuiltInFunctionSupport.kahanSumAbsoluteDeviations(numbers, mean))
                    .divide(BigDecimal.valueOf(size), mathContext);
        }
        BigDecimal sum = ZERO;
        for (BigDecimal number : numbers) {
            sum = sum.add(number);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(size), mathContext);
        BigDecimal deviation = ZERO;
        for (BigDecimal number : numbers) {
            deviation = deviation.add(number.subtract(mean, mathContext).abs(mathContext), mathContext);
        }
        return deviation.divide(BigDecimal.valueOf(size), mathContext);
    }

    public BigDecimal rule3d(BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin2.multiply(result1, mathContext).divide(origin1, mathContext);
    }

    public BigDecimal rule3i(BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin1.multiply(result1, mathContext).divide(origin2, mathContext);
    }

    public List<BigDecimal> distribute(
            BigDecimal value,
            BigDecimal direction,
            List<BigDecimal> targetValues,
            List<BigDecimal> limitValues) {
        BigDecimal[] targets = BuiltInFunctionSupport.numbers(targetValues);
        BigDecimal[] limits = BuiltInFunctionSupport.numbers(limitValues);
        if (targets.length != limits.length) {
            throw new IllegalArgumentException("target and limits vectors must have the same size");
        }
        BigDecimal[] distributed = new BigDecimal[targets.length + 1];
        System.arraycopy(targets, 0, distributed, 0, targets.length);
        BigDecimal currentValue = value;
        if (currentValue.compareTo(ZERO) >= 0) {
            if (direction.compareTo(ZERO) >= 0) {
                for (int index = 0; index < targets.length; index++) {
                    currentValue = positiveDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            } else {
                for (int index = targets.length - 1; index >= 0; index--) {
                    currentValue = positiveDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            }
        } else {
            currentValue = currentValue.abs();
            if (direction.compareTo(ZERO) >= 0) {
                for (int index = 0; index < targets.length; index++) {
                    currentValue = negativeDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            } else {
                for (int index = targets.length - 1; index >= 0; index--) {
                    currentValue = negativeDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            }
            currentValue = currentValue.negate();
        }
        distributed[distributed.length - 1] = currentValue;
        return List.of(distributed);
    }

    public List<BigDecimal> spread(BigDecimal value, BigDecimal direction, List<BigDecimal> referenceValues) {
        BigDecimal[] references = BuiltInFunctionSupport.numbers(referenceValues);
        if (references.length == 0) {
            throw new IllegalArgumentException("references vector must not be empty");
        }
        int scale = value.scale();
        if (value.compareTo(ZERO) == 0) {
            return Collections.nCopies(references.length, ZERO.setScale(scale, HALF_EVEN));
        }
        BigDecimal total = ZERO;
        for (BigDecimal reference : references) {
            total = total.add(reference);
        }
        BigDecimal[] distributed = new BigDecimal[references.length];
        BigDecimal distributedSum = ZERO;
        if (total.compareTo(ZERO) != 0) {
            BigDecimal factor = value.divide(total, mathContext);
            for (int index = 0; index < references.length; index++) {
                BigDecimal distributedValue = references[index].multiply(factor).setScale(scale, HALF_EVEN);
                distributed[index] = distributedValue;
                distributedSum = distributedSum.add(distributedValue);
            }
        } else {
            BigDecimal size = BigDecimal.valueOf(references.length);
            BigDecimal distributedValue = value.divide(size, HALF_EVEN).setScale(scale, HALF_EVEN);
            distributedSum = distributedValue.multiply(size);
            java.util.Arrays.fill(distributed, distributedValue);
        }
        BigDecimal difference = value.subtract(distributedSum).setScale(scale, HALF_EVEN);
        if (difference.compareTo(ZERO) != 0) {
            int adjustmentIndex = -1;
            if (direction.compareTo(ZERO) >= 0) {
                for (int index = 0; index < references.length; index++) {
                    if (references[index].compareTo(ZERO) != 0) {
                        adjustmentIndex = index;
                        break;
                    }
                }
                if (adjustmentIndex == -1) {
                    adjustmentIndex = 0;
                }
            } else {
                for (int index = distributed.length - 1; index >= 0; index--) {
                    if (references[index].compareTo(ZERO) != 0) {
                        adjustmentIndex = index;
                        break;
                    }
                }
                if (adjustmentIndex == -1) {
                    adjustmentIndex = distributed.length - 1;
                }
            }
            distributed[adjustmentIndex] = distributed[adjustmentIndex].add(difference);
        }
        return List.of(distributed);
    }

    private static BigDecimal positiveDistribution(
            BigDecimal value,
            BigDecimal target,
            BigDecimal limit,
            int index,
            BigDecimal[] distributed) {
        if (target.compareTo(limit) >= 0) {
            distributed[index] = target;
            return value;
        }
        BigDecimal capacity = limit.subtract(target);
        if (value.compareTo(capacity) <= 0) {
            distributed[index] = target.add(value);
            return BuiltInFunctionSupport.zeroAtScale(value);
        }
        distributed[index] = limit;
        return value.subtract(capacity);
    }

    private static BigDecimal negativeDistribution(
            BigDecimal value,
            BigDecimal target,
            BigDecimal limit,
            int index,
            BigDecimal[] distributed) {
        if (target.compareTo(limit) <= 0) {
            distributed[index] = target;
            return value;
        }
        BigDecimal capacity = target.subtract(limit);
        if (value.compareTo(capacity) <= 0) {
            distributed[index] = target.subtract(value);
            return BuiltInFunctionSupport.zeroAtScale(value);
        }
        distributed[index] = limit;
        return value.subtract(capacity);
    }
}
