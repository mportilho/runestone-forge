package com.runestone.expeval2.api;

import com.runestone.expeval2.types.ScalarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionFacadeTest {

    @Test
    void shouldComputeMathExpressionsUsingEnvironmentDefaultsAndFunctionBindings() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
            .registerStaticProvider(FunctionFixture.class)
            .registerExternalSymbol("rate", ScalarType.NUMBER, new BigDecimal("0.10"), true)
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
            .registerExternalSymbol("rate", ScalarType.NUMBER, new BigDecimal("0.10"), false)
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
            .registerExternalSymbol("threshold", ScalarType.NUMBER, BigDecimal.TEN, true)
            .build();

        boolean result = LogicalExpression.compile("principal > threshold", environment)
            .setValue("principal", 12)
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
}
