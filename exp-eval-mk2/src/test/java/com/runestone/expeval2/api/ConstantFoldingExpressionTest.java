package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Behavioral tests for constant folding: expressions containing foldable functions with literal
 * arguments must produce the same results and audit trail as non-folded evaluation.
 *
 * <p>Skipped categories:
 * <ul>
 *   <li>Error path / invalid input — compile-time errors are covered by ExpressionCompilerTest;
 *       folding adds no new error paths at runtime.
 *   <li>Concurrency — evaluation is stateless per scope; the Caffeine cache is thread-safe.
 * </ul>
 */
@DisplayName("Constant folding — behavioral correctness")
class ConstantFoldingExpressionTest {

    private static final ExpressionEnvironment ENV =
            ExpressionEnvironment.builder().addMathFunctions().build();

    private static final Offset<BigDecimal> EPSILON = within(new BigDecimal("0.00000000000000000001"));

    // -----------------------------------------------------------------------
    // Numeric correctness
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("compute() returns the correct value for folded expressions")
    class NumericCorrectness {

        @Test
        @DisplayName("ln(1) returns 0")
        void lnOfOneReturnsZero() {
            BigDecimal result = MathExpression.compile("ln(1)", ENV).compute();

            assertThat(result).isCloseTo(BigDecimal.ZERO, EPSILON);
        }

        @Test
        @DisplayName("lb(256) returns 8 — binary logarithm of 256 is exactly 8")
        void lbOf256Returns8() {
            BigDecimal result = MathExpression.compile("lb(256)", ENV).compute();

            assertThat(result).isCloseTo(new BigDecimal("8"), EPSILON);
        }

        @Test
        @DisplayName("ln(1.05) * n — folded constant multiplied by a variable yields the correct product")
        void foldedLnMultipliedByVariableYieldsCorrectProduct() {
            // ln(1.05) ≈ 0.04879016417; 0.04879016417 * 100 ≈ 4.879016417
            BigDecimal result = MathExpression.compile("ln(1.05) * n", ENV)
                    .setValue("n", new BigDecimal("100"))
                    .compute();

            BigDecimal expected = new BigDecimal("100").multiply(
                    MathExpression.compile("ln(1.05)", ENV).compute());
            assertThat(result).isCloseTo(expected, EPSILON);
        }

        @Test
        @DisplayName("folded result matches non-folded result for ln(variable) with the same value")
        void foldedResultMatchesNonFoldedForSameValue() {
            // ln(1.05) as folded literal vs ln(x) where x=1.05 must agree
            BigDecimal foldedResult = MathExpression.compile("ln(1.05)", ENV).compute();
            BigDecimal nonFoldedResult = MathExpression.compile("ln(x)", ENV)
                    .setValue("x", new BigDecimal("1.05"))
                    .compute();

            assertThat(foldedResult).isCloseTo(nonFoldedResult, EPSILON);
        }
    }

    // -----------------------------------------------------------------------
    // Idempotency — repeated compute() calls must be stable
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Idempotency — repeated compute() calls return the same value")
    class Idempotency {

        @Test
        @DisplayName("successive compute() calls on a folded expression return the same result")
        void successiveCallsReturnSameResult() {
            MathExpression expr = MathExpression.compile("ln(1.05)", ENV);

            BigDecimal first = expr.compute();
            BigDecimal second = expr.compute();
            BigDecimal third = expr.compute();

            assertThat(first).isEqualByComparingTo(second);
            assertThat(second).isEqualByComparingTo(third);
        }

        @Test
        @DisplayName("folded constant combined with variable remains stable across calls with same input")
        void foldedWithVariableStableAcrossCalls() {
            MathExpression expr = MathExpression.compile("ln(1.05) * n", ENV)
                    .setValue("n", new BigDecimal("50"));

            BigDecimal first = expr.compute();
            BigDecimal second = expr.compute();

            assertThat(first).isEqualByComparingTo(second);
        }
    }

    // -----------------------------------------------------------------------
    // Audit trail — folded path must emit the same events as normal path
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Audit trail — FunctionCall event is emitted for folded calls")
    class AuditTrail {

        @Test
        @DisplayName("computeWithAudit() emits exactly one FunctionCall event for ln(literal)")
        void foldedCallEmitsOneFunctionCallEvent() {
            AuditResult<BigDecimal> result = MathExpression.compile("ln(1.05)", ENV)
                    .computeWithAudit();

            assertThat(result.trace().functionCalls()).hasSize(1);
        }

        @Test
        @DisplayName("FunctionCall event carries the function name 'ln'")
        void foldedCallEventHasCorrectFunctionName() {
            AuditResult<BigDecimal> result = MathExpression.compile("ln(1.05)", ENV)
                    .computeWithAudit();

            AuditEvent.FunctionCall call = result.trace().functionCalls().getFirst();

            assertThat(call.functionName()).isEqualTo("ln");
        }

        @Test
        @DisplayName("FunctionCall event result matches the computed value")
        void foldedCallEventResultMatchesComputedValue() {
            AuditResult<BigDecimal> result = MathExpression.compile("ln(1.05)", ENV)
                    .computeWithAudit();

            AuditEvent.FunctionCall call = result.trace().functionCalls().getFirst();

            assertThat((BigDecimal) call.result())
                    .isCloseTo(result.value(), EPSILON);
        }

        @Test
        @DisplayName("FunctionCall event carries non-empty input arguments")
        void foldedCallEventCarriesInputArgs() {
            AuditResult<BigDecimal> result = MathExpression.compile("ln(1.05)", ENV)
                    .computeWithAudit();

            AuditEvent.FunctionCall call = result.trace().functionCalls().getFirst();

            assertThat(call.inputArgs()).isNotEmpty();
        }

        @Test
        @DisplayName("computeWithAudit() value for folded expression equals compute() value")
        void auditValueMatchesComputeValue() {
            MathExpression expr = MathExpression.compile("ln(1.05)", ENV);

            BigDecimal direct = expr.compute();
            BigDecimal audited = expr.computeWithAudit().value();

            assertThat(audited).isCloseTo(direct, EPSILON);
        }

        @Test
        @DisplayName("successive computeWithAudit() calls produce independent trace instances")
        void successiveAuditCallsProduceIndependentTraces() {
            MathExpression expr = MathExpression.compile("ln(1.05)", ENV);

            AuditResult<BigDecimal> first = expr.computeWithAudit();
            AuditResult<BigDecimal> second = expr.computeWithAudit();

            assertThat(first.trace()).isNotSameAs(second.trace());
            assertThat(first.trace().events()).isNotSameAs(second.trace().events());
        }

        @Test
        @DisplayName("two folded function calls in one expression emit two FunctionCall events")
        void twoFoldedCallsEmitTwoEvents() {
            // ln(1.05) + lb(256) — both are folded; both must appear in the audit
            AuditResult<BigDecimal> result = MathExpression.compile("ln(1.05) + lb(256)", ENV)
                    .computeWithAudit();

            List<AuditEvent.FunctionCall> calls = result.trace().functionCalls();
            assertThat(calls).hasSize(2)
                    .extracting(AuditEvent.FunctionCall::functionName)
                    .containsExactlyInAnyOrder("ln", "lb");
        }

        @Test
        @DisplayName("ln(variable) still emits a FunctionCall event via the normal (non-folded) path")
        void nonFoldedCallAlsoEmitsFunctionCallEvent() {
            AuditResult<BigDecimal> result = MathExpression.compile("ln(x)", ENV)
                    .setValue("x", new BigDecimal("1.05"))
                    .computeWithAudit();

            assertThat(result.trace().functionCalls()).hasSize(1);
            assertThat(result.trace().functionCalls().getFirst().functionName()).isEqualTo("ln");
        }
    }
}
