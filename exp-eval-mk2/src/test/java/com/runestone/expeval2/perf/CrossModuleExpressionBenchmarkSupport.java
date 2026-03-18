package com.runestone.expeval2.perf;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval2.api.MathExpression;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.StringJoiner;

public final class CrossModuleExpressionBenchmarkSupport {

    private static final int FRAME_COUNT = 256;
    private static final MethodType WEIGHTED_TYPE = MethodType.methodType(
        BigDecimal.class,
        BigDecimal.class,
        BigDecimal.class,
        BigDecimal.class
    );
    private static final String[] VARIABLE_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};

    public static final String LITERAL_DENSE_EXPRESSION = buildLiteralDenseExpression();
    public static final String VARIABLE_CHURN_EXPRESSION =
        "a * b + c * d - e + f * g - h + i * j + k - l + (a * c) + (b * d)";
    public static final String USER_FUNCTION_EXPRESSION =
        "weighted(a, b, c) + weighted(d, e, f) + weighted(g, h, i) + weighted(j, k, l)";

    private static final BigDecimal[] LITERAL_SEEDS = buildLiteralSeeds();
    private static final Frame[] VARIABLE_FRAMES = buildFrames(3L, 11L);
    private static final Frame[] USER_FUNCTION_FRAMES = buildFrames(17L, 23L);

    private CrossModuleExpressionBenchmarkSupport() {
    }

    public static Expression newLegacyLiteralDenseExpression() {
        return new Expression(LITERAL_DENSE_EXPRESSION);
    }

    public static MathExpression newMk2LiteralDenseExpression() {
        return MathExpression.compile(LITERAL_DENSE_EXPRESSION);
    }

    public static Expression newLegacyVariableChurnExpression() {
        return new Expression(VARIABLE_CHURN_EXPRESSION);
    }

    public static MathExpression newMk2VariableChurnExpression() {
        return MathExpression.compile(VARIABLE_CHURN_EXPRESSION);
    }

    public static Expression newLegacyUserFunctionExpression() {
        Expression expression = new Expression(USER_FUNCTION_EXPRESSION);
        expression.addFunction("weighted", WEIGHTED_TYPE, args -> CustomFunctionFixture.weighted(
            (BigDecimal) args[0],
            (BigDecimal) args[1],
            (BigDecimal) args[2]
        ));
        return expression;
    }

    public static MathExpression newMk2UserFunctionExpression() {
        return MathExpression.compile(USER_FUNCTION_EXPRESSION, userFunctionEnvironment());
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

    public static void applyLiteralSeed(Expression expression, BigDecimal seed) {
        expression.setVariable("seed", seed);
    }

    public static void applyLiteralSeed(MathExpression expression, BigDecimal seed) {
        expression.setValue("seed", seed);
    }

    public static void applyFrame(Expression expression, Frame frame) {
        for (int index = 0; index < VARIABLE_NAMES.length; index++) {
            expression.setVariable(VARIABLE_NAMES[index], frame.values[index]);
        }
    }

    public static void applyFrame(MathExpression expression, Frame frame) {
        for (int index = 0; index < VARIABLE_NAMES.length; index++) {
            expression.setValue(VARIABLE_NAMES[index], frame.values[index]);
        }
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
