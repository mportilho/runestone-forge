package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            .compute(Map.of("principal", 200));

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldRejectInternalAssignmentsFromSetValue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        MathExpression expression = MathExpression.compile("fee = 10; fee + principal", environment);

        assertThatThrownBy(() -> expression.compute(Map.of("fee", 20)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("internal");
    }

    @Test
    void shouldRejectNonOverridableEnvironmentSymbols() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerExternalSymbol("rate", new BigDecimal("0.10"), false)
            .build();
        MathExpression expression = MathExpression.compile("principal * rate", environment);

        assertThatThrownBy(() -> expression.compute(Map.of("principal", 100, "rate", 0.20)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not overridable");
    }

    @Test
    void shouldComputeLogicalExpressionsWithDefaultedExternalSymbols() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerExternalSymbol("threshold", BigDecimal.TEN, true)
            .build();

        boolean result = LogicalExpression.compile("principal > threshold", environment)
            .compute(Map.of("principal", 12));

        assertThat(result).isTrue();
    }

    @Test
    void shouldComputeAssignmentExpressionsUsingOnlyEnvironmentDefaults() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerExternalSymbol("a", BigDecimal.ONE, true)
            .registerExternalSymbol("b", new BigDecimal("2"), true)
            .build();

        Map<String, Object> result = AssignmentExpression.compile("x = a + b; y = x + 1;", environment)
            .compute();

        assertThat(result)
            .containsEntry("x", new BigDecimal("3"))
            .containsEntry("y", new BigDecimal("4"));
    }

    @Test
    void shouldLetAssignmentExpressionsOverrideSubsetOfEnvironmentDefaults() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerExternalSymbol("a", BigDecimal.ONE, true)
            .registerExternalSymbol("b", new BigDecimal("2"), true)
            .registerExternalSymbol("c", new BigDecimal("3"), true)
            .build();

        Map<String, Object> result = AssignmentExpression.compile("x = a + b; y = x + c;", environment)
            .compute(Map.of("a", 10));

        assertThat(result)
            .containsEntry("x", new BigDecimal("12"))
            .containsEntry("y", new BigDecimal("15"));
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
        Map<String, Object> vals = new HashMap<>();
        vals.put("x", null);
        boolean result = LogicalExpression.compile("x = 1", environment).compute(vals);
        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleNullSelfEquality() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        Map<String, Object> vals = new HashMap<>();
        vals.put("x", null);
        vals.put("y", null);
        boolean result = LogicalExpression.compile("x = y", environment).compute(vals);
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleVectorEqualityWithDifferentScales() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        boolean result = LogicalExpression.compile("x = y", environment)
            .compute(Map.of("x", List.of(new BigDecimal("1")), "y", List.of(new BigDecimal("1.0"))));
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleVectorEqualityWithNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        boolean result = LogicalExpression.compile("x = y", environment)
            .compute(Map.of(
                "x", Arrays.asList(null, new BigDecimal("1")),
                "y", Arrays.asList(null, new BigDecimal("1.0"))
            ));
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
