package com.runestone.expeval.api;

import com.runestone.expeval.api.support.TestCollectionFunctions;
import com.runestone.expeval.api.support.TestScaleProvider;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code ..funcName(args)} collection-function syntax:
 * custom catalog functions invoked with the navigated collection/map as the
 * implicit first argument, optional extra arguments, and optional
 * {@link MathContext} auto-binding.
 *
 * <p>Provider classes ({@link TestCollectionFunctions}, {@link TestScaleProvider}) are
 * public top-level classes so that {@link java.lang.invoke.MethodHandles#lookup()} from
 * inside the library can resolve their method handles without access errors.
 */
@DisplayName("Collection navigation — custom catalog functions")
class CollectionNavigationCustomFunctionTest {

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------

    private static final List<BigDecimal> PRICES = List.of(
            new BigDecimal("5"),
            new BigDecimal("15"),
            new BigDecimal("25"),
            new BigDecimal("10")
    );

    private static ExpressionEnvironment staticEnv() {
        return ExpressionEnvironment.builder()
                .registerStaticProvider(TestCollectionFunctions.class)
                .registerExternalSymbol("prices", PRICES, true)
                .build();
    }

    // -------------------------------------------------------------------------
    // No-extra-argument function
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Function with no extra arguments ..distinctCount()")
    class NoExtraArgFunction {

        @Test
        @DisplayName("counts all elements when they are all distinct")
        void shouldCountDistinctElements() {
            BigDecimal result = MathExpression.compile("prices..distinctCount()", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("4");
        }

        @Test
        @DisplayName("counts only unique values when duplicates are present")
        void shouldIgnoreDuplicates() {
            var repeated = List.of(
                    new BigDecimal("5"), new BigDecimal("15"),
                    new BigDecimal("5"), new BigDecimal("15"));
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerStaticProvider(TestCollectionFunctions.class)
                    .registerExternalSymbol("nums", repeated, true)
                    .build();
            BigDecimal result = MathExpression.compile("nums..distinctCount()", e)
                    .compute(Map.of("nums", repeated));
            assertThat(result).isEqualByComparingTo("2");
        }
    }

    // -------------------------------------------------------------------------
    // Function with one extra argument
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Function with one extra argument ..countAbove(threshold)")
    class ExtraArgFunction {

        @Test
        @DisplayName("counts elements strictly above the threshold")
        void shouldCountElementsAboveThreshold() {
            // PRICES = [5, 15, 25, 10] → above 10: [15, 25] → 2
            BigDecimal result = MathExpression.compile("prices..countAbove(10)", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("returns 0 when no elements exceed the threshold")
        void shouldReturnZeroWhenNothingAboveThreshold() {
            // Nothing above 100
            BigDecimal result = MathExpression.compile("prices..countAbove(100)", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("threshold can come from an external symbol variable")
        void shouldAcceptThresholdFromExternalVariable() {
            // threshold bound at compute time
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerStaticProvider(TestCollectionFunctions.class)
                    .registerExternalSymbol("prices", PRICES, true)
                    .registerExternalSymbol("thr", new BigDecimal("5"), true)
                    .build();
            // 15, 25, 10 are all > 5 → 3
            BigDecimal result = MathExpression.compile("prices..countAbove(thr)", e)
                    .compute(Map.of("prices", PRICES, "thr", new BigDecimal("5")));
            assertThat(result).isEqualByComparingTo("3");
        }
    }

    // -------------------------------------------------------------------------
    // Function that returns a List (chainable)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Function returning a List — result can be further chained")
    class FunctionReturningList {

        @Test
        @DisplayName("..top(n) returns the n largest elements and can be aggregated with ..sum()")
        void shouldChainTopWithSum() {
            // top(2) of [5, 15, 25, 10] → [25, 15] → sum = 40
            BigDecimal result = MathExpression.compile("prices..top(2)..sum()", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("40");
        }

        @Test
        @DisplayName("..top(n) result can be counted with ..count()")
        void shouldChainTopWithCount() {
            // top(3) of 4 elements → list of 3 → count = 3
            BigDecimal result = MathExpression.compile("prices..top(3)..count()", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("..top(n) result can be further filtered before aggregation")
        void shouldChainTopWithFilterAndCount() {
            // top(3) → [25, 15, 10]; filter > 12 → [25, 15] → count = 2
            BigDecimal result = MathExpression.compile(
                    "prices..top(3)[?(@ > 12)]..count()", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("..top(n) where n exceeds list size returns all elements")
        void shouldReturnAllWhenNExceedsSize() {
            // top(99) of 4 elements → all 4 → sum = 55
            BigDecimal result = MathExpression.compile("prices..top(99)..sum()", staticEnv())
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("55");
        }
    }

    // -------------------------------------------------------------------------
    // MathContext auto-binding
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("MathContext auto-binding — leading MathContext parameter is bound from the environment")
    class MathContextAutoBinding {

        @Test
        @DisplayName("function uses the environment's math context for division")
        void shouldApplyEnvironmentMathContextToDivision() {
            // With precision=4: 55 / 3 = 18.33 (4 significant digits)
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerStaticProvider(TestCollectionFunctions.class)
                    .registerExternalSymbol("prices", PRICES, true)
                    .withMathContext(new MathContext(4))
                    .build();
            BigDecimal result = MathExpression.compile("prices..normalizedSum(3)", e)
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("18.33");
        }

        @Test
        @DisplayName("different environments with different precision produce differently rounded results")
        void shouldProduceDifferentResultForDifferentPrecision() {
            // precision=6: 55 / 3 = 18.3333 (6 significant digits)
            ExpressionEnvironment e6 = ExpressionEnvironment.builder()
                    .registerStaticProvider(TestCollectionFunctions.class)
                    .registerExternalSymbol("prices", PRICES, true)
                    .withMathContext(new MathContext(6))
                    .build();
            BigDecimal result = MathExpression.compile("prices..normalizedSum(3)", e6)
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("18.3333");
        }

        @Test
        @DisplayName("function computes correctly when divisor comes from an external symbol")
        void shouldAcceptDivisorAsExternalSymbol() {
            // 55 / 5 = 11 exactly — no rounding needed
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerStaticProvider(TestCollectionFunctions.class)
                    .registerExternalSymbol("prices", PRICES, true)
                    .registerExternalSymbol("div", new BigDecimal("5"), true)
                    .build();
            BigDecimal result = MathExpression.compile("prices..normalizedSum(div)", e)
                    .compute(Map.of("prices", PRICES, "div", new BigDecimal("5")));
            assertThat(result).isEqualByComparingTo("11");
        }
    }

    // -------------------------------------------------------------------------
    // Instance provider
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Instance provider — non-static methods registered via registerInstanceProvider")
    class InstanceProviderFunction {

        @Test
        @DisplayName("instance method captures state from the provider object")
        void shouldUseInstanceStateInFunction() {
            // ScaleProvider(factor=2): scaleAndSum([5,15,25,10]) = 5*2+15*2+25*2+10*2 = 110
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new TestScaleProvider(new BigDecimal("2")))
                    .registerExternalSymbol("prices", PRICES, true)
                    .build();
            BigDecimal result = MathExpression.compile("prices..scaleAndSum()", e)
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("110");
        }

        @Test
        @DisplayName("different instances of the provider produce different results")
        void shouldReflectDifferentInstanceStates() {
            // ScaleProvider(factor=3): sum of 55 * 3 = 165
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new TestScaleProvider(new BigDecimal("3")))
                    .registerExternalSymbol("prices", PRICES, true)
                    .build();
            BigDecimal result = MathExpression.compile("prices..scaleAndSum()", e)
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("165");
        }
    }

    // -------------------------------------------------------------------------
    // Error paths
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Error paths — unknown function or arity mismatch at compile time")
    class ErrorPaths {

        @Test
        @DisplayName("unknown function name produces a compilation error")
        void shouldFailCompilationForUnknownFunction() {
            assertThatThrownBy(() ->
                    MathExpression.compile("prices..nonExistentFunc()", staticEnv()))
                    .isInstanceOf(ExpressionCompilationException.class)
                    .hasMessageContaining("UNKNOWN_COLLECTION_FUNCTION");
        }

        @Test
        @DisplayName("wrong arity — extra argument on a zero-arg function produces a compilation error")
        void shouldFailCompilationForExtraArgument() {
            // distinctCount takes only (List); passing an extra arg raises arity mismatch
            assertThatThrownBy(() ->
                    MathExpression.compile("prices..distinctCount(99)", staticEnv()))
                    .isInstanceOf(ExpressionCompilationException.class)
                    .hasMessageContaining("INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS");
        }

        @Test
        @DisplayName("missing required argument produces a compilation error")
        void shouldFailCompilationForMissingArgument() {
            // countAbove requires (List, threshold); calling without threshold is an arity error
            assertThatThrownBy(() ->
                    MathExpression.compile("prices..countAbove()", staticEnv()))
                    .isInstanceOf(ExpressionCompilationException.class)
                    .hasMessageContaining("INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS");
        }
    }
}
