package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
                    .compute(Map.of("n", new BigDecimal("100")));

            BigDecimal expected = new BigDecimal("100").multiply(
                    MathExpression.compile("ln(1.05)", ENV).compute());
            assertThat(result).isCloseTo(expected, EPSILON);
        }

        @Test
        @DisplayName("ln(lb(256)) — nested foldable calls produce the correct value (lb=8, ln(8)≈2.079)")
        void nestedFoldedCallsProduceCorrectValue() {
            BigDecimal result = MathExpression.compile("ln(lb(256))", ENV).compute();

            // lb(256)=8, ln(8)=ln(2^3)=3*ln(2)≈2.0794415417
            BigDecimal expected = MathExpression.compile("ln(8)", ENV).compute();
            assertThat(result).isCloseTo(expected, EPSILON);
        }

        @Test
        @DisplayName("log(lb(4), lb(256)) — two nested foldable args produce the correct value (log₂(8)=3)")
        void nestedFoldedTwoArgsProduceCorrectValue() {
            BigDecimal result = MathExpression.compile("log(lb(4), lb(256))", ENV).compute();

            assertThat(result).isCloseTo(new BigDecimal("3"), EPSILON);
        }

        @Test
        @DisplayName("folded result matches non-folded result for ln(variable) with the same value")
        void foldedResultMatchesNonFoldedForSameValue() {
            // ln(1.05) as folded literal vs ln(x) where x=1.05 must agree
            BigDecimal foldedResult = MathExpression.compile("ln(1.05)", ENV).compute();
            BigDecimal nonFoldedResult = MathExpression.compile("ln(x)", ENV)
                    .compute(Map.of("x", new BigDecimal("1.05")));

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
            MathExpression expr = MathExpression.compile("ln(1.05) * n", ENV);
            Map<String, Object> vars = Map.of("n", new BigDecimal("50"));

            BigDecimal first = expr.compute(vars);
            BigDecimal second = expr.compute(vars);

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
            // x + ln(1.05) + y + lb(256) — both function calls are folded; both must appear in the audit
            AuditResult<BigDecimal> result = MathExpression.compile("x + ln(1.05) + y + lb(256)", ENV)
                    .computeWithAudit(Map.of("x", BigDecimal.ONE, "y", BigDecimal.ONE));

            List<AuditEvent.FunctionCall> calls = result.trace().functionCalls();
            assertThat(calls).hasSize(2)
                    .extracting(AuditEvent.FunctionCall::functionName)
                    .containsExactlyInAnyOrder("ln", "lb");
        }

        @Test
        @DisplayName("ln(variable) still emits a FunctionCall event via the normal (non-folded) path")
        void nonFoldedCallAlsoEmitsFunctionCallEvent() {
            AuditResult<BigDecimal> result = MathExpression.compile("ln(x)", ENV)
                    .computeWithAudit(Map.of("x", new BigDecimal("1.05")));

            assertThat(result.trace().functionCalls()).hasSize(1);
            assertThat(result.trace().functionCalls().getFirst().functionName()).isEqualTo("ln");
        }
    }

    // -----------------------------------------------------------------------
    // Runtime invocation count — the core folding contract
    // -----------------------------------------------------------------------

    /**
     * These tests verify that a foldable function is called exactly once (at build time)
     * and never again during {@code compute()}, regardless of how many times it is called.
     *
     * <p>Each test uses a fresh {@link ExpressionCompiler} to prevent the Caffeine cache from
     * returning a previously compiled expression that would skip the build-time invocation.
     */
    @Nested
    @DisplayName("Runtime invocation count — foldable function must not be called during compute()")
    class RuntimeInvocationCount {

        @BeforeEach
        void resetCounter() {
            CountedFunctions.CALL_COUNT.set(0);
        }

        @Test
        @DisplayName("foldable function with literal arg is invoked exactly once at build time, zero times during compute()")
        void foldableFunctionInvokedOnceAtBuildTimeNeverDuringCompute() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(CountedFunctions.class, true)
                    .build();
            ExpressionCompiler freshCompiler = new ExpressionCompiler();

            MathExpression expr = MathExpression.compile("counted(5)", env, freshCompiler);
            assertThat(CountedFunctions.CALL_COUNT.get())
                    .as("function must be called exactly once at fold time")
                    .isEqualTo(1);

            expr.compute();
            expr.compute();
            expr.compute();
            assertThat(CountedFunctions.CALL_COUNT.get())
                    .as("function must not be called again during compute()")
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("non-foldable function with literal arg is invoked on every compute() call")
        void nonFoldableFunctionInvokedOnEachCompute() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(CountedFunctions.class)   // foldable=false
                    .build();
            ExpressionCompiler freshCompiler = new ExpressionCompiler();

            MathExpression expr = MathExpression.compile("counted(5)", env, freshCompiler);
            assertThat(CountedFunctions.CALL_COUNT.get())
                    .as("non-foldable function must not be called at build time")
                    .isEqualTo(0);

            expr.compute();
            expr.compute();
            expr.compute();
            assertThat(CountedFunctions.CALL_COUNT.get())
                    .as("non-foldable function must be called once per compute()")
                    .isEqualTo(3);
        }

        @Test
        @DisplayName("nested foldable calls counted(counted(5)) are both invoked exactly once at build time")
        void nestedFoldableCallsInvokedTwiceAtBuildTimeNeverDuringCompute() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(CountedFunctions.class, true)
                    .build();
            ExpressionCompiler freshCompiler = new ExpressionCompiler();

            // inner counted(5) folds first (1 call), outer counted(result) folds next (1 call) = 2 total
            MathExpression expr = MathExpression.compile("counted(counted(5))", env, freshCompiler);
            assertThat(CountedFunctions.CALL_COUNT.get())
                    .as("both inner and outer calls must be invoked at fold time")
                    .isEqualTo(2);

            expr.compute();
            expr.compute();
            assertThat(CountedFunctions.CALL_COUNT.get())
                    .as("neither call must be invoked during compute()")
                    .isEqualTo(2);
        }
    }

    // -----------------------------------------------------------------------
    // Constant propagation — variable assigned a constant is substituted
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Constant propagation — internal variable assigned a constant value is inlined")
    class ConstantPropagation {

        // Concurrency: foldedSymbols is local to build() — thread-safe by design; no concurrent tests needed.
        // Null: `x = null` is invalid syntax (rejected at parse time); null propagation is not applicable.

        // --- Happy path ---

        @Test
        @DisplayName("x = 10; x + 5 computes 15")
        void simpleVariablePropagation() {
            BigDecimal result = MathExpression.compile("x = 10; x + 5", ENV).compute();

            assertThat(result).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("x = 2; y = x * 3; y + x computes 8 (two-level chain)")
        void twoLevelChainedPropagation() {
            BigDecimal result = MathExpression.compile("x = 2; y = x * 3; y + x", ENV).compute();

            assertThat(result).isEqualByComparingTo("8");
        }

        @Test
        @DisplayName("x = 2; y = x * 3; z = y + x; z computes 8 (three-level chain)")
        void threeLevelChainedPropagation() {
            BigDecimal result = MathExpression.compile("x = 2; y = x * 3; z = y + x; z", ENV).compute();

            assertThat(result).isEqualByComparingTo("8");
        }

        @Test
        @DisplayName("x = 5; x * x computes 25 — variable used multiple times in the result")
        void variableUsedMultipleTimesInResult() {
            BigDecimal result = MathExpression.compile("x = 5; x * x", ENV).compute();

            assertThat(result).isEqualByComparingTo("25");
        }

        @Test
        @DisplayName("x = 10; x + n computes correctly when external n is provided")
        void propagatedConstantCombinedWithExternal() {
            BigDecimal result = MathExpression.compile("x = 10; x + n", ENV)
                    .compute(Map.of("n", new BigDecimal("5")));

            assertThat(result).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("x = 10; ln(x) propagates x into the function and folds it")
        void propagatedConstantAllowsFunctionFolding() {
            BigDecimal result = MathExpression.compile("x = 10; ln(x)", ENV).compute();
            BigDecimal expected = MathExpression.compile("ln(10)", ENV).compute();

            assertThat(result).isCloseTo(expected, EPSILON);
        }

        @Test
        @DisplayName("x = 10; y = ln(x); y + 1 — chain through foldable function produces correct value")
        void propagationThroughFoldableFunction() {
            BigDecimal result = MathExpression.compile("x = 10; y = ln(x); y + 1", ENV).compute();
            BigDecimal expected = MathExpression.compile("ln(10) + 1", ENV).compute();

            assertThat(result).isCloseTo(expected, EPSILON);
        }

        // --- Boundary / edge ---

        @ParameterizedTest(name = "x = {0}; x + 0 returns {0}")
        @CsvSource({
            "0,    0",
            "1,    1",
            "-1,   -1",
            "100,  100",
            "0.5,  0.5"
        })
        @DisplayName("x = <value>; x + 0 — propagation preserves boundary numeric values")
        void boundaryNumericValues(String value, String expected) {
            BigDecimal result = MathExpression.compile("x = " + value + "; x + 0", ENV).compute();

            assertThat(result).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("x = 10; if (x > 5) then 100 else 0 endif — propagation folds conditional to then-branch")
        void propagationFoldsConditionalBranch() {
            BigDecimal result = MathExpression.compile("x = 10; if (x > 5) then 100 else 0 endif", ENV).compute();

            assertThat(result).isEqualByComparingTo("100");
        }

        // --- State transitions (re-assignment) ---

        @Test
        @DisplayName("x = 1; x = n; x + 3 — re-assignment to non-constant, runtime value of n is used")
        void reAssignmentToNonConstantUsesRuntimeValue() {
            BigDecimal result = MathExpression.compile("x = 1; x = n; x + 3", ENV)
                    .compute(Map.of("n", new BigDecimal("7")));

            // Must be 10 (7 + 3), NOT 4 (old constant 1 + 3)
            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("x = 1; x = x + 2; x — re-assignment to new constant updates propagated value to 3")
        void reAssignmentToNewConstantPropagatesUpdatedValue() {
            BigDecimal result = MathExpression.compile("x = 1; x = x + 2; x", ENV).compute();

            assertThat(result).isEqualByComparingTo("3");
        }

        // --- Variable depending on external (not folded — runtime path) ---

        @Test
        @DisplayName("x = n + 1; x + 5 computes correctly at runtime when x depends on an external")
        void variableDependingOnExternalEvaluatesCorrectly() {
            BigDecimal result = MathExpression.compile("x = n + 1; x + 5", ENV)
                    .compute(Map.of("n", new BigDecimal("4")));

            assertThat(result).isEqualByComparingTo("10");
        }

        // --- Type coverage ---

        @Test
        @DisplayName("x = 10; y = 5; x > y computes true (numeric propagation in logical expression)")
        void numericPropagationInLogicalExpression() {
            boolean result = LogicalExpression.compile("x = 10; y = 5; x > y", ENV).compute();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("x = 10; y = 5; x between y and 20 computes true (propagation into ternary operator)")
        void propagationIntoTernaryOperator() {
            boolean result = LogicalExpression.compile("x = 10; y = 5; x between y and 20", ENV).compute();

            assertThat(result).isTrue();
        }

        // --- Idempotency ---

        @Test
        @DisplayName("successive compute() calls on a propagated expression return the same result")
        void successiveCallsReturnSameResult() {
            MathExpression expr = MathExpression.compile("x = 2; y = x * 3; y + x", ENV);

            BigDecimal first = expr.compute();
            BigDecimal second = expr.compute();
            BigDecimal third = expr.compute();

            assertThat(first).isEqualByComparingTo(second);
            assertThat(second).isEqualByComparingTo(third);
            assertThat(first).isEqualByComparingTo("8");
        }
    }

    /** Fixture that counts how many times it has been invoked. */
    public static final class CountedFunctions {

        static final AtomicInteger CALL_COUNT = new AtomicInteger(0);

        public static BigDecimal counted(BigDecimal value) {
            CALL_COUNT.incrementAndGet();
            return value;
        }
    }
}
