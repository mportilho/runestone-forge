package com.runestone.expeval.perf;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class ExpressionBenchmarkSupport {

    private static final int FRAME_COUNT = 256;
    private static final String[] VARIABLE_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};

    public static final String LITERAL_DENSE_EXPRESSION = buildLiteralDenseExpression();
    public static final String VARIABLE_CHURN_EXPRESSION =
        "a * b + c * d - e + f * g - h + i * j + k - l + (a * c) + (b * d)";
    public static final String USER_FUNCTION_EXPRESSION =
        "weighted(a, b, c) + weighted(d, e, f) + weighted(g, h, i) + weighted(j, k, l)";
    public static final String CONDITIONAL_EXPRESSION =
        "if a > b then a * c - d + e - f else g * h - i + j * k - l endif";
    public static final String LOGARITHM_CHAIN_EXPRESSION =
        "ln(a) + ln(b) + ln(c) + ln(d) + ln(e) + ln(f) + lb(g) + lb(h) + lb(i) + lb(j) + lb(k) + lb(l)";
    public static final String POWER_CHAIN_EXPRESSION =
        "a^2 + b^2 - c^2 + d^2 - e^2 + f^2 + g^2 - h^2 + i^2 - j^2 + k^2 - l^2";

    private static final BigDecimal[] LITERAL_SEEDS = buildLiteralSeeds();
    private static final Frame[] VARIABLE_FRAMES = buildFrames(3L, 11L);
    private static final Frame[] USER_FUNCTION_FRAMES = buildFrames(17L, 23L);

    private ExpressionBenchmarkSupport() {
    }

    public static MathExpression newLiteralDenseExpression() {
        return MathExpression.compile(LITERAL_DENSE_EXPRESSION);
    }

    public static MathExpression newVariableChurnExpression() {
        return MathExpression.compile(VARIABLE_CHURN_EXPRESSION);
    }

    public static MathExpression newUserFunctionExpression() {
        return MathExpression.compile(USER_FUNCTION_EXPRESSION, userFunctionEnvironment());
    }

    public static MathExpression newConditionalExpression() {
        return MathExpression.compile(CONDITIONAL_EXPRESSION);
    }

    public static MathExpression newLogarithmChainExpression() {
        return MathExpression.compile(LOGARITHM_CHAIN_EXPRESSION, logarithmEnvironment());
    }

    public static ExpressionEnvironment emptyEnvironment() {
        return ExpressionEnvironmentBuilder.empty();
    }

    public static ExpressionEnvironment logarithmEnvironment() {
        return ExpressionEnvironment.builder()
            .addMathFunctions()
            .build();
    }

    public static MathExpression newLogarithmChainExpressionDecimal64() {
        return MathExpression.compile(LOGARITHM_CHAIN_EXPRESSION, logarithmEnvironmentDecimal64());
    }

    public static ExpressionEnvironment logarithmEnvironmentDecimal64() {
        return ExpressionEnvironment.builder()
            .addMathFunctions()
            .withMathContext(MathContext.DECIMAL64)
            .build();
    }

    public static MathExpression newPowerChainExpression() {
        return MathExpression.compile(POWER_CHAIN_EXPRESSION);
    }

    public static BigDecimal literalSeed(int index) {
        return LITERAL_SEEDS[index & (FRAME_COUNT - 1)];
    }

    public static Frame variableFrame(int index) {
        return VARIABLE_FRAMES[index & (FRAME_COUNT - 1)];
    }

    public static Frame userFunctionFrame(int index) {
        return USER_FUNCTION_FRAMES[index & (FRAME_COUNT - 1)];
    }

    public static Map<String, Object> literalSeedToMap(BigDecimal seed) {
        return Map.of("seed", seed);
    }

    public static Map<String, Object> frameToMap(Frame frame) {
        Map<String, Object> map = new HashMap<>(VARIABLE_NAMES.length * 2);
        for (int index = 0; index < VARIABLE_NAMES.length; index++) {
            map.put(VARIABLE_NAMES[index], frame.values[index]);
        }
        return map;
    }

    public static ExpressionEnvironment userFunctionEnvironment() {
        return ExpressionEnvironment.builder()
            .registerStaticProvider(CustomFunctionFixture.class)
            .build();
    }

    private static String buildLiteralDenseExpression() {
        StringJoiner joiner = new StringJoiner(" + ");
        joiner.add("seed");
        for (int index = 1; index <= 64; index++) {
            joiner.add("100%d.%04d".formatted(index, index));
        }
        return joiner.toString();
    }

    private static BigDecimal[] buildLiteralSeeds() {
        BigDecimal[] values = new BigDecimal[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            values[index] = new BigDecimal("%d.%03d".formatted(index + 1, (index * 37) % 1000));
        }
        return values;
    }

    private static Frame[] buildFrames(long baseOffset, long multiplier) {
        Frame[] frames = new Frame[FRAME_COUNT];
        for (int frameIndex = 0; frameIndex < FRAME_COUNT; frameIndex++) {
            BigDecimal[] values = new BigDecimal[VARIABLE_NAMES.length];
            for (int valueIndex = 0; valueIndex < VARIABLE_NAMES.length; valueIndex++) {
                long integerPart = baseOffset + frameIndex + (valueIndex + 1L) * multiplier;
                long fractionalPart = (frameIndex * 53L + valueIndex * 17L) % 1000L;
                values[valueIndex] = new BigDecimal("%d.%03d".formatted(integerPart, fractionalPart));
            }
            frames[frameIndex] = new Frame(values);
        }
        return frames;
    }

    public static final class CustomFunctionFixture {

        private static final BigDecimal HALF = new BigDecimal("0.5");
        private static final BigDecimal ONE_POINT_FIVE = new BigDecimal("1.5");
        private static final BigDecimal QUARTER = new BigDecimal("0.25");

        private CustomFunctionFixture() {
        }

        public static BigDecimal weighted(BigDecimal left, BigDecimal middle, BigDecimal right) {
            return left.multiply(HALF)
                .add(middle.multiply(ONE_POINT_FIVE))
                .subtract(right.multiply(QUARTER));
        }
    }

    public static final class Frame {

        private final BigDecimal[] values;

        private Frame(BigDecimal[] values) {
            this.values = values;
        }

        public BigDecimal valueAt(int index) {
            return values[index];
        }
    }
}
