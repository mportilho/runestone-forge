package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionFacadeTest {

    @Test
    void shouldComputeMathExpressionsUsingEnvironmentDefaultsAndFunctionBindings() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerStaticProvider(FunctionFixture.class)
            .registerExternalSymbol("rate", new BigDecimal("0.10"), true)
            .build();

        BigDecimal result = MathExpression.compile("bonus(principal) * rate", environment)
            .setValue("principal", 200)
            .compute();

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldRejectInternalAssignmentsFromSetValue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        MathExpression expression = MathExpression.compile("fee = 10; fee + principal", environment);

        assertThatThrownBy(() -> expression.setValue("fee", 20))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("internal");
    }

    @Test
    void shouldRejectNonOverridableEnvironmentSymbols() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerExternalSymbol("rate", new BigDecimal("0.10"), false)
            .build();
        MathExpression expression = MathExpression.compile("principal * rate", environment)
            .setValue("principal", 100);

        assertThatThrownBy(() -> expression.setValue("rate", 0.20))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not overridable");
    }

    @Test
    void shouldComputeLogicalExpressionsWithDefaultedExternalSymbols() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerExternalSymbol("threshold", BigDecimal.TEN, true)
            .build();

        boolean result = LogicalExpression.compile("principal > threshold", environment)
            .setValue("principal", 12)
            .compute();

        assertThat(result).isTrue();
    }

    @Test
    void shouldComputePowWithNonIntegerExponentAtDecimal128Precision() {
        // double gives 1.4142135623730951 (rounds up at the 17th decimal place)
        // DECIMAL128 correctly gives 1.41421356237309504880... (true value)
        BigDecimal result = MathExpression.compile("2 ^ 0.5", ExpressionEnvironmentBuilder.empty()).compute();

        assertThat(result.toPlainString()).startsWith("1.41421356237309504");
    }

    @Test
    void shouldComputeSqrtAtDecimal128Precision() {
        BigDecimal result = MathExpression.compile("sqrt(2)", ExpressionEnvironmentBuilder.empty()).compute();

        assertThat(result.toPlainString()).startsWith("1.41421356237309504");
    }

    @Test
    void shouldComputeRootAtDecimal128Precision() {
        BigDecimal result = MathExpression.compile("2 root 2", ExpressionEnvironmentBuilder.empty()).compute();

        assertThat(result.toPlainString()).startsWith("1.41421356237309504");
    }

    @Test
    void shouldShortCircuitAndWhenLeftIsFalse() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerStaticProvider(FailingFixture.class)
            .build();

        boolean result = LogicalExpression.compile("false and alwaysFails()", environment).compute();

        assertThat(result).isFalse();
    }

    @Test
    void shouldShortCircuitOrWhenLeftIsTrue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerStaticProvider(FailingFixture.class)
            .build();

        boolean result = LogicalExpression.compile("true or alwaysFails()", environment).compute();

        assertThat(result).isTrue();
    }

    @Test
    void shouldShortCircuitNandWhenLeftIsFalse() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerStaticProvider(FailingFixture.class)
            .build();

        boolean result = LogicalExpression.compile("false nand alwaysFails()", environment).compute();

        assertThat(result).isTrue();
    }

    @Test
    void shouldShortCircuitNorWhenLeftIsTrue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerStaticProvider(FailingFixture.class)
            .build();

        boolean result = LogicalExpression.compile("true nor alwaysFails()", environment).compute();

        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleNullEquality() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        boolean result = LogicalExpression.compile("x = 1", environment)
            .setValue("x", null)
            .compute();
        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleNullSelfEquality() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        boolean result = LogicalExpression.compile("x = y", environment)
            .setValue("x", null)
            .setValue("y", null)
            .compute();
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleVectorEqualityWithDifferentScales() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        boolean result = LogicalExpression.compile("x = y", environment)
            .setValue("x", List.of(new BigDecimal("1")))
            .setValue("y", List.of(new BigDecimal("1.0")))
            .compute();
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleVectorEqualityWithNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        boolean result = LogicalExpression.compile("x = y", environment)
            .setValue("x", java.util.Arrays.asList(null, new BigDecimal("1")))
            .setValue("y", java.util.Arrays.asList(null, new BigDecimal("1.0")))
            .compute();
        assertThat(result).isTrue();
    }

    public static final class FunctionFixture {

        private FunctionFixture() {
        }

        public static BigDecimal bonus(BigDecimal principal) {
            return principal.multiply(new BigDecimal("0.5"));
        }
    }

    public static final class FailingFixture {

        private FailingFixture() {
        }

        public static boolean alwaysFails() {
            throw new AssertionError("right operand must not be evaluated due to short-circuit");
        }
    }
}
