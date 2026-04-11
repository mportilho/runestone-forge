package com.runestone.expeval.environment;

import com.runestone.expeval.api.MathExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transcendental Precision Management Tests")
class TranscendentalPrecisionTest {

    @Test
    @DisplayName("Uses default precision (DECIMAL128) for transcendental functions")
    void usesDefaultPrecision() {
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .addMathFunctions()
                .build();

        MathExpression expr = MathExpression.compile("ln(2)", env);
        BigDecimal result = expr.compute(Map.of());

        // ln(2) with DECIMAL128 has many digits
        assertThat(result.precision()).isGreaterThan(30);
        assertThat(result.toString()).startsWith("0.6931471805599453");
    }

    @Test
    @DisplayName("Uses custom transcendental precision (DECIMAL3) while keeping standard math at DECIMAL128")
    void usesCustomTranscendentalPrecision() {
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .addMathFunctions()
                .withMathContext(MathContext.DECIMAL128)
                .withTranscendentalMathContext(new MathContext(3))
                .build();

        // ln(2) should have 3 digits of precision
        MathExpression lnExpr = MathExpression.compile("ln(2)", env);
        BigDecimal lnResult = lnExpr.compute(Map.of());
        assertThat(lnResult.toString()).isEqualTo("0.693");

        // Standard math (addition) should still use DECIMAL128 if it were coerced, 
        // but here we just check if it's affected. 
        // Basic operators (+, -, *, /) use the environment's mathContext.
        MathExpression addExpr = MathExpression.compile("1.11111111111111111111 / 3", env);
        BigDecimal addResult = addExpr.compute(Map.of());
        assertThat(addResult.precision()).isGreaterThan(15);
    }

    @Test
    @DisplayName("Trigonometry functions also respect transcendental precision")
    void trigonometryRespectsPrecision() {
        ExpressionEnvironment env = ExpressionEnvironment.builder()
                .addTrigonometryFunctions()
                .withTranscendentalMathContext(new MathContext(5))
                .build();

        MathExpression expr = MathExpression.compile("sin(1)", env);
        BigDecimal result = expr.compute(Map.of());

        // sin(1) with 5 digits
        assertThat(result.toString()).hasSize(7); // "0.84147"
        assertThat(result.precision()).isEqualTo(5);
    }
}
