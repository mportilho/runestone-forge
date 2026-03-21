package com.runestone.expeval2.catalog.functions;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

@DisplayName("MathFunctions Tests")
class MathFunctionsTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.0000000000001"));

    private static BigDecimal[] bdArray(String... values) {
        BigDecimal[] arr = new BigDecimal[values.length];
        for (int i = 0; i < values.length; i++) {
            arr[i] = new BigDecimal(values[i]);
        }
        return arr;
    }

    @Nested
    @DisplayName("mean()")
    class MeanTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.mean(MC, null));
        }

        @Test
        @DisplayName("Throws exception on empty array")
        void throwsOnEmptyArray() {
            BigDecimal[] input = bdArray();
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.mean(MC, input));
        }

        @Test
        @DisplayName("Single value")
        void singleValue() {
            BigDecimal[] input = bdArray("5.5");
            assertThat(MathFunctions.mean(MC, input)).isEqualByComparingTo("5.5");
        }

        @Test
        @DisplayName("Two values")
        void twoValues() {
            BigDecimal[] input = bdArray("10", "20");
            assertThat(MathFunctions.mean(MC, input)).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("Multiple values")
        void multipleValues() {
            BigDecimal[] input = bdArray("10", "20", "30", "40");
            assertThat(MathFunctions.mean(MC, input)).isEqualByComparingTo("25");
        }
    }

    @Nested
    @DisplayName("geometricMean()")
    class GeometricMeanTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.geometricMean(MC, null));
        }

        @Test
        @DisplayName("Handles empty array")
        void handlesEmptyArray() {
            BigDecimal[] input = bdArray();
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.geometricMean(MC, input));
        }

        @Test
        @DisplayName("Positive numbers")
        void positiveNumbers() {
            BigDecimal[] input = bdArray("2", "8"); // root(16, 2) = 4
            assertThat(MathFunctions.geometricMean(MC, input)).isCloseTo(new BigDecimal("4"), EPSILON);
        }

        @Test
        @DisplayName("Multiple values")
        void multipleValues() {
            BigDecimal[] inputDec = bdArray("4", "1", "0.03125"); // 4 * 1 * 0.03125 = 0.125. root(0.125, 3) = 0.5
            assertThat(MathFunctions.geometricMean(MC, inputDec)).isCloseTo(new BigDecimal("0.5"), EPSILON);
        }
    }

    @Nested
    @DisplayName("harmonicMean()")
    class HarmonicMeanTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.harmonicMean(MC, null));
        }

        @Test
        @DisplayName("Throws exception if array contains zero")
        void throwsOnZeroValue() {
            BigDecimal[] input = bdArray("1", "0", "4");
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.harmonicMean(MC, input));
        }

        @Test
        @DisplayName("Throws exception on empty array")
        void throwsOnEmptyArray() {
            BigDecimal[] input = bdArray();
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.harmonicMean(MC, input));
        }

        @Test
        @DisplayName("Positive numbers")
        void positiveNumbers() {
            BigDecimal[] input = bdArray("1", "4", "4"); // 3 / (1/1 + 1/4 + 1/4) = 3 / 1.5 = 2
            assertThat(MathFunctions.harmonicMean(MC, input)).isCloseTo(new BigDecimal("2"), EPSILON);
        }
    }

    @Nested
    @DisplayName("variance()")
    class VarianceTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.variance(MC, null, 0));
        }

        @Test
        @DisplayName("Throws exception when array length equals type (division by zero)")
        void throwsOnLengthEqualsType() {
            BigDecimal[] input = bdArray("4"); // length 1
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.variance(MC, input, 1));
        }

        @Test
        @DisplayName("Population variance (type 0)")
        void populationVariance() {
            BigDecimal[] input = bdArray("4", "8", "6", "5", "3", "2", "8", "9", "2", "5");
            assertThat(MathFunctions.variance(MC, input, 0)).isCloseTo(new BigDecimal("5.76"), EPSILON);
        }

        @Test
        @DisplayName("Sample variance (type 1)")
        void sampleVariance() {
            BigDecimal[] input = bdArray("4", "8", "6", "5", "3", "2", "8", "9", "2", "5");
            assertThat(MathFunctions.variance(MC, input, 1)).isCloseTo(new BigDecimal("6.4"), EPSILON);
        }
    }

    @Nested
    @DisplayName("stdDev()")
    class StdDevTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.stdDev(MC, null, 0));
        }

        @Test
        @DisplayName("Throws exception when array length equals type (division by zero)")
        void throwsOnLengthEqualsType() {
            BigDecimal[] input = bdArray("4"); // length 1
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.stdDev(MC, input, 1));
        }

        @Test
        @DisplayName("Population standard deviation (type 0)")
        void populationStdDev() {
            BigDecimal[] input = bdArray("4", "8", "6", "5", "3", "2", "8", "9", "2", "5");
            assertThat(MathFunctions.stdDev(MC, input, 0)).isCloseTo(new BigDecimal("2.4"), EPSILON);
        }

        @Test
        @DisplayName("Sample standard deviation (type 1)")
        void sampleStdDev() {
            BigDecimal[] input = bdArray("4", "8", "6", "5", "3", "2", "8", "9", "2", "5");
            // sqrt(6.4) approx 2.52982212813
            assertThat(MathFunctions.stdDev(MC, input, 1)).isCloseTo(new BigDecimal("2.5298221281347035"), EPSILON);
        }
    }

    @Nested
    @DisplayName("meanDev()")
    class MeanDevTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.meanDev(MC, null));
        }

        @Test
        @DisplayName("Throws exception on empty array")
        void throwsOnEmptyArray() {
            BigDecimal[] input = bdArray();
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.meanDev(MC, input));
        }

        @Test
        @DisplayName("Calculates mean deviation correctly")
        void calculatesMeanDeviation() {
            BigDecimal[] input = bdArray("3", "6", "6", "7", "8", "11", "15", "16");
            // mean = 72 / 8 = 9
            // diffs = 6, 3, 3, 2, 1, 2, 6, 7
            // sum diffs = 30
            // mean dev = 30 / 8 = 3.75
            assertThat(MathFunctions.meanDev(MC, input)).isCloseTo(new BigDecimal("3.75"), EPSILON);
        }
    }

    @Nested
    @DisplayName("ln()")
    class LnTests {

        @Test
        @DisplayName("Calculates natural logarithm correctly")
        void ln() {
            assertThat(MathFunctions.ln(MC, new BigDecimal("1"))).isCloseTo(ZERO, EPSILON);
            assertThat(MathFunctions.ln(MC, new BigDecimal("2.718281828459"))).isCloseTo(ONE, EPSILON);
            assertThat(MathFunctions.ln(MC, new BigDecimal("10"))).isCloseTo(new BigDecimal("2.302585092994046"), EPSILON);
        }
    }

    @Nested
    @DisplayName("lb()")
    class LbTests {

        @Test
        @DisplayName("Calculates binary logarithm correctly")
        void lb() {
            assertThat(MathFunctions.lb(MC, new BigDecimal("1"))).isCloseTo(ZERO, EPSILON);
            assertThat(MathFunctions.lb(MC, new BigDecimal("2"))).isCloseTo(ONE, EPSILON);
            assertThat(MathFunctions.lb(MC, new BigDecimal("4"))).isCloseTo(new BigDecimal("2"), EPSILON);
            assertThat(MathFunctions.lb(MC, new BigDecimal("10"))).isCloseTo(new BigDecimal("3.321928094887362"), EPSILON);
        }
    }

    @Nested
    @DisplayName("log()")
    class LogTests {

        @Test
        @DisplayName("Calculates logarithm with specific base correctly")
        void log() {
            assertThat(MathFunctions.log(MC, new BigDecimal("10"), new BigDecimal("1"))).isCloseTo(ZERO, EPSILON);
            assertThat(MathFunctions.log(MC, new BigDecimal("10"), new BigDecimal("10"))).isCloseTo(ONE, EPSILON);
            assertThat(MathFunctions.log(MC, new BigDecimal("10"), new BigDecimal("100"))).isCloseTo(new BigDecimal("2"), EPSILON);
            assertThat(MathFunctions.log(MC, new BigDecimal("2"), new BigDecimal("8"))).isCloseTo(new BigDecimal("3"), EPSILON);
            assertThat(MathFunctions.log(MC, new BigDecimal("3"), new BigDecimal("81"))).isCloseTo(new BigDecimal("4"), EPSILON);
        }
    }

    @Nested
    @DisplayName("lnFast()")
    class LnFastTests {

        private static final Offset<Double> DOUBLE_EPSILON = within(1e-10);

        @ParameterizedTest(name = "lnFast({0}) = {1}")
        @CsvSource({
                "1.0,  0.0",
                "2.718281828459045, 1.0",
                "10.0, 2.302585092994046",
                "0.5, -0.6931471805599453"
        })
        @DisplayName("Happy path: known values")
        void happyPath(double input, double expected) {
            assertThat(MathFunctions.lnFast(input)).isCloseTo(expected, DOUBLE_EPSILON);
        }

        @Test
        @DisplayName("Zero returns NEGATIVE_INFINITY")
        void zeroReturnsNegativeInfinity() {
            assertThat(MathFunctions.lnFast(0.0)).isEqualTo(Double.NEGATIVE_INFINITY);
        }

        @Test
        @DisplayName("Negative value returns NaN")
        void negativeReturnsNaN() {
            assertThat(MathFunctions.lnFast(-1.0)).isNaN();
        }
    }

    @Nested
    @DisplayName("lbFast()")
    class LbFastTests {

        private static final Offset<Double> DOUBLE_EPSILON = within(1e-10);

        @ParameterizedTest(name = "lbFast({0}) = {1}")
        @CsvSource({
                "1.0, 0.0",
                "2.0, 1.0",
                "4.0, 2.0",
                "8.0, 3.0",
                "10.0, 3.321928094887362"
        })
        @DisplayName("Happy path: known values")
        void happyPath(double input, double expected) {
            assertThat(MathFunctions.lbFast(input)).isCloseTo(expected, DOUBLE_EPSILON);
        }

        @Test
        @DisplayName("Zero returns NEGATIVE_INFINITY")
        void zeroReturnsNegativeInfinity() {
            assertThat(MathFunctions.lbFast(0.0)).isEqualTo(Double.NEGATIVE_INFINITY);
        }

        @Test
        @DisplayName("Negative value returns NaN")
        void negativeReturnsNaN() {
            assertThat(MathFunctions.lbFast(-1.0)).isNaN();
        }
    }

    @Nested
    @DisplayName("logFast()")
    class LogFastTests {

        private static final Offset<Double> DOUBLE_EPSILON = within(1e-10);

        @ParameterizedTest(name = "logFast({0}, {1}) = {2}")
        @CsvSource({
                "10.0, 1.0,   0.0",
                "10.0, 10.0,  1.0",
                "10.0, 100.0, 2.0",
                "2.0,  8.0,   3.0",
                "3.0,  81.0,  4.0"
        })
        @DisplayName("Happy path: known values")
        void happyPath(double base, double value, double expected) {
            assertThat(MathFunctions.logFast(base, value)).isCloseTo(expected, DOUBLE_EPSILON);
        }

        @Test
        @DisplayName("Value zero returns NEGATIVE_INFINITY")
        void valueZeroReturnsNegativeInfinity() {
            assertThat(MathFunctions.logFast(10.0, 0.0)).isEqualTo(Double.NEGATIVE_INFINITY);
        }

        @Test
        @DisplayName("Negative value returns NaN")
        void negativeValueReturnsNaN() {
            assertThat(MathFunctions.logFast(10.0, -1.0)).isNaN();
        }

        @Test
        @DisplayName("Negative base returns NaN")
        void negativeBaseReturnsNaN() {
            assertThat(MathFunctions.logFast(-2.0, 10.0)).isNaN();
        }

        @Test
        @DisplayName("Base 1 returns POSITIVE_INFINITY (log(1) = 0, positive / 0.0 in IEEE 754)")
        void baseOneReturnsInfinity() {
            assertThat(MathFunctions.logFast(1.0, 10.0)).isEqualTo(Double.POSITIVE_INFINITY);
        }
    }

    @Nested
    @DisplayName("rule3d()")
    class Rule3dTests {

        @Test
        @DisplayName("Throws exception on null arguments")
        void throwsOnNullArguments() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.rule3d(MC, null, ONE, ONE));
        }

        @Test
        @DisplayName("Throws exception on zero origin1")
        void throwsOnZeroOrigin1() {
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.rule3d(MC, ZERO, ONE, ONE));
        }

        @Test
        @DisplayName("Calculates direct rule of three")
        void calculatesDirectRuleOfThree() {
            // If 2 items cost 10, how much do 5 items cost? (5 * 10) / 2 = 25
            BigDecimal origin1 = new BigDecimal("2");
            BigDecimal result1 = new BigDecimal("10");
            BigDecimal origin2 = new BigDecimal("5");
            assertThat(MathFunctions.rule3d(MC, origin1, result1, origin2)).isEqualByComparingTo("25");
        }
    }

    @Nested
    @DisplayName("rule3i()")
    class Rule3iTests {

        @Test
        @DisplayName("Throws exception on null arguments")
        void throwsOnNullArguments() {
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.rule3i(MC, ONE, ONE, null));
        }

        @Test
        @DisplayName("Throws exception on zero origin2")
        void throwsOnZeroOrigin2() {
            Assertions.assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> MathFunctions.rule3i(MC, ONE, ONE, ZERO));
        }

        @Test
        @DisplayName("Calculates inverse rule of three")
        void calculatesInverseRuleOfThree() {
            // Formula: (origin1 * result1) / origin2
            // Ex: 4 workers take 6 days to build a wall. How many days for 8 workers?
            // (4 * 6) / 8 = 3 days
            BigDecimal origin1 = new BigDecimal("4");
            BigDecimal result1 = new BigDecimal("6");
            BigDecimal origin2 = new BigDecimal("8");
            assertThat(MathFunctions.rule3i(MC, origin1, result1, origin2)).isEqualByComparingTo("3");
        }
    }

    @Nested
    @DisplayName("distribute()")
    class DistributeTests {

        @Test
        @DisplayName("Throws exception if value is null")
        void throwsOnNullValue() {
            BigDecimal[] target = bdArray("1");
            BigDecimal[] limits = bdArray("2");
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.distribute(null, ONE, target, limits))
                    .withMessage("Value cannot be null");
        }

        @Test
        @DisplayName("Throws exception if direction is null")
        void throwsOnNullDirection() {
            BigDecimal[] target = bdArray("1");
            BigDecimal[] limits = bdArray("2");
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.distribute(ONE, null, target, limits))
                    .withMessage("Direction cannot be null");
        }

        @Test
        @DisplayName("Throws exception if target is null")
        void throwsOnNullTarget() {
            BigDecimal[] limits = bdArray("2");
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.distribute(ONE, ONE, null, limits))
                    .withMessage("Target cannot be null");
        }

        @Test
        @DisplayName("Throws exception if limits is null")
        void throwsOnNullLimits() {
            BigDecimal[] target = bdArray("1");
            Assertions.assertThatNullPointerException()
                    .isThrownBy(() -> MathFunctions.distribute(ONE, ONE, target, null))
                    .withMessage("Limits cannot be null");
        }

        @Test
        @DisplayName("Throws exception when limits length differs from target")
        void throwsOnDifferentArrayLengths() {
            BigDecimal[] target = bdArray("1", "2");
            BigDecimal[] limits = bdArray("2");
            Assertions.assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
                    .isThrownBy(() -> MathFunctions.distribute(new BigDecimal("10"), ONE, target, limits));
        }

        @Test
        @DisplayName("Distributes positive value forwards")
        void positiveForward() {
            BigDecimal value = new BigDecimal("5");
            BigDecimal direction = ONE;
            BigDecimal[] target = bdArray("1", "2", "3");
            BigDecimal[] limits = bdArray("4", "4", "4");

            // distribute 5:
            // i=0: t=1, l=4 -> add 3 to t[0], remaining=2, t[0]=4
            // i=1: t=2, l=4 -> add 2 to t[1], remaining=0, t[1]=4
            // i=2: t=3, l=4 -> unchanged
            // remainder=0
            BigDecimal[] result = MathFunctions.distribute(value, direction, target, limits);
            
            assertThat(result).hasSize(4);
            assertThat(result[0]).isEqualByComparingTo("4");
            assertThat(result[1]).isEqualByComparingTo("4");
            assertThat(result[2]).isEqualByComparingTo("3");
            assertThat(result[3]).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Distributes positive value backwards")
        void positiveBackward() {
            BigDecimal value = new BigDecimal("5");
            BigDecimal direction = new BigDecimal("-1");
            BigDecimal[] target = bdArray("1", "2", "3");
            BigDecimal[] limits = bdArray("4", "4", "4");

            // distribute 5 backwards:
            // i=2: t=3, l=4 -> add 1, remaining=4, t[2]=4
            // i=1: t=2, l=4 -> add 2, remaining=2, t[1]=4
            // i=0: t=1, l=4 -> add 2, remaining=0, t[0]=3
            // remainder=0
            BigDecimal[] result = MathFunctions.distribute(value, direction, target, limits);

            assertThat(result).hasSize(4);
            assertThat(result[0]).isEqualByComparingTo("3");
            assertThat(result[1]).isEqualByComparingTo("4");
            assertThat(result[2]).isEqualByComparingTo("4");
            assertThat(result[3]).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Distributes negative value forwards")
        void negativeForward() {
            BigDecimal value = new BigDecimal("-5");
            BigDecimal direction = ONE;
            BigDecimal[] target = bdArray("4", "3", "2");
            BigDecimal[] limits = bdArray("1", "1", "1");

            // distribute -5 forwards (abs val = 5):
            // i=0: t=4, l=1 -> sub 3, remaining=2, t[0]=1
            // i=1: t=3, l=1 -> sub 2, remaining=0, t[1]=1
            // i=2: t=2, l=1 -> unchanged
            // remainder=0
            BigDecimal[] result = MathFunctions.distribute(value, direction, target, limits);

            assertThat(result).hasSize(4);
            assertThat(result[0]).isEqualByComparingTo("1");
            assertThat(result[1]).isEqualByComparingTo("1");
            assertThat(result[2]).isEqualByComparingTo("2");
            assertThat(result[3]).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Distributes negative value backwards")
        void negativeBackward() {
            BigDecimal value = new BigDecimal("-5");
            BigDecimal direction = new BigDecimal("-1");
            BigDecimal[] target = bdArray("4", "3", "2");
            BigDecimal[] limits = bdArray("1", "1", "1");

            // distribute -5 backwards (abs=5):
            // i=2: t=2, l=1 -> sub 1, rem=4, t[2]=1
            // i=1: t=3, l=1 -> sub 2, rem=2, t[1]=1
            // i=0: t=4, l=1 -> sub 2, rem=0, t[0]=2
            // remainder=0
            BigDecimal[] result = MathFunctions.distribute(value, direction, target, limits);

            assertThat(result).hasSize(4);
            assertThat(result[0]).isEqualByComparingTo("2");
            assertThat(result[1]).isEqualByComparingTo("1");
            assertThat(result[2]).isEqualByComparingTo("1");
            assertThat(result[3]).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Returns remainder when limits are reached")
        void positiveWithRemainder() {
            BigDecimal value = new BigDecimal("10");
            BigDecimal direction = ONE;
            BigDecimal[] target = bdArray("1", "2");
            BigDecimal[] limits = bdArray("3", "3");

            // distribute 10:
            // i=0: t=1, l=3 -> add 2, rem=8, t[0]=3
            // i=1: t=2, l=3 -> add 1, rem=7, t[1]=3
            // remainder=7
            BigDecimal[] result = MathFunctions.distribute(value, direction, target, limits);

            assertThat(result).hasSize(3);
            assertThat(result[0]).isEqualByComparingTo("3");
            assertThat(result[1]).isEqualByComparingTo("3");
            assertThat(result[2]).isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("Returns negative remainder when limits are reached")
        void negativeWithRemainder() {
            BigDecimal value = new BigDecimal("-10");
            BigDecimal direction = ONE;
            BigDecimal[] target = bdArray("3", "3");
            BigDecimal[] limits = bdArray("1", "2");

            // distribute -10 (abs=10):
            // i=0: t=3, l=1 -> sub 2, rem=8, t[0]=1
            // i=1: t=3, l=2 -> sub 1, rem=7, t[1]=2
            // remainder=-7
            BigDecimal[] result = MathFunctions.distribute(value, direction, target, limits);

            assertThat(result).hasSize(3);
            assertThat(result[0]).isEqualByComparingTo("1");
            assertThat(result[1]).isEqualByComparingTo("2");
            assertThat(result[2]).isEqualByComparingTo("-7");
        }

        @Test
        @DisplayName("Ignores buckets that already violate positive limit")
        void positiveAlreadyViolated() {
            // target=15, limit=10. Adding 5. Should ignore bucket 0.
            BigDecimal value = new BigDecimal("5");
            BigDecimal[] target = bdArray("15");
            BigDecimal[] limits = bdArray("10");
            BigDecimal[] result = MathFunctions.distribute(value, ONE, target, limits);

            assertThat(result[0]).isEqualByComparingTo("15");
            assertThat(result[1]).isEqualByComparingTo("5");
        }

        @Test
        @DisplayName("Ignores buckets that already violate negative limit (floor)")
        void negativeAlreadyViolated() {
            // target=5, limit=10 (floor). Subtracting 2. Should ignore bucket 0.
            BigDecimal value = new BigDecimal("-2");
            BigDecimal[] target = bdArray("5");
            BigDecimal[] limits = bdArray("10");
            BigDecimal[] result = MathFunctions.distribute(value, ONE, target, limits);

            assertThat(result[0]).isEqualByComparingTo("5");
            assertThat(result[1]).isEqualByComparingTo("-2");
        }
    }

    @Nested
    @DisplayName("spread()")
    class SpreadTests {

        @Test
        @DisplayName("Throws exception if value is null")
        void throwsOnNullValue() {
            BigDecimal[] refs = bdArray("1");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathFunctions.spread(MC, null, ONE, refs))
                    .withMessage("Value cannot be null");
        }

        @Test
        @DisplayName("Throws exception if references are null")
        void throwsOnNullReferences() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathFunctions.spread(MC, ONE, ONE, null))
                    .withMessage("References cannot be null or empty");
        }

        @Test
        @DisplayName("Throws exception if references are empty")
        void throwsOnEmptyReferences() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathFunctions.spread(MC, ONE, ONE, new BigDecimal[0]))
                    .withMessage("References cannot be null or empty");
        }

        @Test
        @DisplayName("Zero value returns array of zeros")
        void zeroValue() {
            BigDecimal[] result = MathFunctions.spread(MC, new BigDecimal("0.00"), ONE, bdArray("1", "2"));
            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualByComparingTo("0.00");
            assertThat(result[1]).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Spreads negative value proportionally")
        void negativeValueSpread() {
            BigDecimal value = new BigDecimal("-10.00");
            BigDecimal[] refs = bdArray("1", "1", "1");

            // Total = 3. -10 / 3 = -3.33 each. Sum = -9.99. Diff = -0.01
            // Distributed forwards, index 0 gets the -0.01.
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);
            assertThat(result).hasSize(3);
            assertThat(result[0]).isEqualByComparingTo("-3.34");
            assertThat(result[1]).isEqualByComparingTo("-3.33");
            assertThat(result[2]).isEqualByComparingTo("-3.33");
            
            BigDecimal sum = ZERO;
            for (BigDecimal r : result) sum = sum.add(r);
            assertThat(sum).isEqualByComparingTo("-10.00");
        }

        @Test
        @DisplayName("Handles mixed sign references")
        void mixedSignReferences() {
            BigDecimal value = new BigDecimal("100");
            BigDecimal[] refs = bdArray("10", "-5");

            // TotalSum = 5
            // i=0: 10 / 5 * 100 = 200
            // i=1: -5 / 5 * 100 = -100
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);
            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualByComparingTo("200");
            assertThat(result[1]).isEqualByComparingTo("-100");
            
            BigDecimal sum = ZERO;
            for (BigDecimal r : result) sum = sum.add(r);
            assertThat(sum).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("Handles zero sum with negative references")
        void zeroSumWithNegatives() {
            BigDecimal value = new BigDecimal("10.0");
            BigDecimal[] refs = bdArray("10", "-10");

            // TotalSum = 0. Equal distribution.
            // 10 / 2 = 5.0 each
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);
            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualByComparingTo("5.0");
            assertThat(result[1]).isEqualByComparingTo("5.0");
        }

        @Test
        @DisplayName("Spreads proportionately based on references")
        void proportionalSpread() {
            BigDecimal value = new BigDecimal("100");
            BigDecimal[] refs = bdArray("1", "3");

            // Total = 4.
            // i=0: 100 * (1/4) = 25
            // i=1: 100 * (3/4) = 75
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);
            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualByComparingTo("25");
            assertThat(result[1]).isEqualByComparingTo("75");
        }

        @Test
        @DisplayName("Applies remainder forwards when difference exists")
        void forwardsDifference() {
            BigDecimal value = new BigDecimal("10.00");
            BigDecimal[] refs = bdArray("1", "1", "1");

            // Total = 3. 10 / 3 = 3.33 each. Sum = 9.99. Diff = 0.01
            // Distributed forwards, index 0 gets the 0.01.
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);
            assertThat(result).hasSize(3);
            assertThat(result[0]).isEqualByComparingTo("3.34");
            assertThat(result[1]).isEqualByComparingTo("3.33");
            assertThat(result[2]).isEqualByComparingTo("3.33");
        }

        @Test
        @DisplayName("Applies remainder backwards when direction is negative")
        void backwardsDifference() {
            BigDecimal value = new BigDecimal("10.00");
            BigDecimal[] refs = bdArray("1", "1", "1");

            // Total = 3. 10 / 3 = 3.33 each. Sum = 9.99. Diff = 0.01
            // Distributed backwards, index 2 gets the 0.01.
            BigDecimal[] result = MathFunctions.spread(MC, value, new BigDecimal("-1"), refs);
            assertThat(result).hasSize(3);
            assertThat(result[0]).isEqualByComparingTo("3.33");
            assertThat(result[1]).isEqualByComparingTo("3.33");
            assertThat(result[2]).isEqualByComparingTo("3.34");
        }

        @Test
        @DisplayName("Evenly distributes when reference sum is zero")
        void zeroTotalSum() {
            BigDecimal value = new BigDecimal("10.0");
            BigDecimal[] refs = bdArray("0", "0");

            // Total sum is 0. Falls back to equal division.
            // 10 / 2 = 5.0
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);
            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualByComparingTo("5.0");
            assertThat(result[1]).isEqualByComparingTo("5.0");
        }

        @Test
        @DisplayName("Does not lose residual when all proportional parts round to zero")
        void smallValueRoundingResidual() {
            // value = 0.01, refs = [10, 10, 10].
            // 0.01 * (1/3) = 0.0033... which rounds to 0.00 in scale 2.
            // Sum = 0.00, Diff = 0.01. Should be applied to index 0.
            BigDecimal value = new BigDecimal("0.01");
            BigDecimal[] refs = bdArray("10", "10", "10");
            BigDecimal[] result = MathFunctions.spread(MC, value, ONE, refs);

            assertThat(result).hasSize(3);
            assertThat(result[0]).isEqualByComparingTo("0.01");
            assertThat(result[1]).isEqualByComparingTo("0.00");
            assertThat(result[2]).isEqualByComparingTo("0.00");
            
            BigDecimal sum = ZERO;
            for (BigDecimal r : result) sum = sum.add(r);
            assertThat(sum).isEqualByComparingTo("0.01");
        }
    }
}