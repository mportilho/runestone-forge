package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Destructuring assignment — [a,b,c] = vectorOrFunction; resultExpression")
class DestructuringAssignmentExpressionTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addMathFunctions().build();

    // -------------------------------------------------------------------------
    // Destructuring from a vector literal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("from vector literal")
    class FromVectorLiteral {

        @Test
        @DisplayName("[x,y] = [10,20]; x+y — both slots usable in result expression")
        void bothSlotsUsableInResult() {
            BigDecimal result = MathExpression.compile("[x, y] = [10, 20]; x + y",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isCloseTo(new BigDecimal("30"), EPSILON);
        }

        @Test
        @DisplayName("[a] = [42]; a — single-element destructuring")
        void singleElement() {
            BigDecimal result = MathExpression.compile("[a] = [42]; a",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isCloseTo(new BigDecimal("42"), EPSILON);
        }

        @Test
        @DisplayName("[a,b] = [10,20,30]; a+b — excess source elements are ignored")
        void excessSourceElementsIgnored() {
            BigDecimal result = MathExpression.compile("[a, b] = [10, 20, 30]; a + b",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isCloseTo(new BigDecimal("30"), EPSILON);
        }

        @Test
        @DisplayName("[a,b,c] = [10,20]; a+b — excess targets become null, unused in result")
        void excessTargetsAreNullButUnusedInResult() {
            // c is bound to NullValue; only a+b is evaluated
            BigDecimal result = MathExpression.compile("[a, b, c] = [10, 20]; a + b",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isCloseTo(new BigDecimal("30"), EPSILON);
        }

        @Test
        @DisplayName("[a,b,c] = [10,20,30]; 0 — expression evaluates its final term independently")
        void resultExpressionIsIndependentOfDestructuredVars() {
            BigDecimal result = MathExpression.compile("[a, b, c] = [10, 20, 30]; 0",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isCloseTo(new BigDecimal("0"), EPSILON);
        }
    }

    // -------------------------------------------------------------------------
    // Destructuring from spread()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("from spread() — proportional distribution across reference weights")
    class FromSpread {

        @Test
        @DisplayName("[a,b,c] = spread(100,-1,[100,10,40]); 0 — user's canonical example returns result term")
        void canonicalExampleReturnsResultTerm() {
            // spread distributes 100 proportionally over refs [100,10,40]; result expression is 0
            BigDecimal result = MathExpression.compile(
                    "[pagtoPrincipal, pagtoCorrecao, pagtoJuros] = spread(100, -1, [100, 10, 40]); 0", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("0"), EPSILON);
        }

        @Test
        @DisplayName("[a,b,c] = spread(100,-1,[100,10,40]); a+b+c — sum of slots equals original value")
        void slotsSumToOriginalValue() {
            // spread always preserves the total — a+b+c must equal 100
            BigDecimal result = MathExpression.compile(
                    "[a, b, c] = spread(100, -1, [100, 10, 40]); a + b + c", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("100"), EPSILON);
        }

        @Test
        @DisplayName("[a,b] = spread(100,1,[1,3]); a — forward direction assigns first slot proportionally")
        void forwardDirectionFirstSlot() {
            // refs [1,3], total=4, factor=25 → a=25, b=75
            BigDecimal result = MathExpression.compile("[a, b] = spread(100, 1, [1, 3]); a", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("25"), EPSILON);
        }

        @Test
        @DisplayName("[a,b] = spread(100,1,[1,3]); b — forward direction assigns second slot proportionally")
        void forwardDirectionSecondSlot() {
            BigDecimal result = MathExpression.compile("[a, b] = spread(100, 1, [1, 3]); b", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("75"), EPSILON);
        }

        @Test
        @DisplayName("[a,b] = spread(100,1,[1,1]); a — equal references split evenly")
        void equalReferencesEvenSplit() {
            BigDecimal result = MathExpression.compile("[a, b] = spread(100, 1, [1, 1]); a", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("50"), EPSILON);
        }

        @Test
        @DisplayName("[a,b] = spread(0,1,[1,3]); a+b — zero value yields all-zero slots")
        void zeroValueAllSlotsZero() {
            BigDecimal result = MathExpression.compile("[a, b] = spread(0, 1, [1, 3]); a + b", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("0"), EPSILON);
        }
    }

    // -------------------------------------------------------------------------
    // Multiple consecutive destructuring assignments
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("multiple consecutive destructuring assignments")
    class MultipleDestructuringAssignments {

        @Test
        @DisplayName("two vector-literal blocks — all four slots live in result")
        void twoBlocksAllSlotsLive() {
            BigDecimal result = MathExpression.compile(
                    "[a, b] = [1, 2]; [c, d] = [3, 4]; a + b + c + d",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isCloseTo(new BigDecimal("10"), EPSILON);
        }

        @Test
        @DisplayName("spread block followed by scalar assignment — both contribute to result")
        void spreadBlockFollowedByScalarAssignment() {
            // [a,b] = spread(100,1,[1,3]) → a=25, b=75; fee = 5; a + b + fee = 105
            BigDecimal result = MathExpression.compile(
                    "[a, b] = spread(100, 1, [1, 3]); fee = 5; a + b + fee", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("105"), EPSILON);
        }
    }

    // -------------------------------------------------------------------------
    // Destructuring inside a LogicalExpression
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("in logical expressions")
    class InLogicalExpressions {

        @Test
        @DisplayName("[a,b] = spread(100,1,[1,3]); a < b — smaller slot is less than larger slot")
        void smallerSlotLessThanLarger() {
            // a=25, b=75 → 25 < 75 → true
            boolean result = LogicalExpression.compile("[a, b] = spread(100, 1, [1, 3]); a < b", ENV).compute();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[a,b] = [10,20]; a < b — destructured literal vars usable in logical comparison")
        void literalDestructuringInLogicalExpression() {
            boolean result = LogicalExpression.compile("[a, b] = [10, 20]; a < b",
                    ExpressionEnvironmentBuilder.empty()).compute();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[a,b,c] = spread(100,-1,[100,10,40]); a+b+c = 100 — sum-preservation as logical check")
        void sumPreservationLogicalCheck() {
            boolean result = LogicalExpression.compile(
                    "[a, b, c] = spread(100, -1, [100, 10, 40]); a + b + c = 100", ENV).compute();
            assertThat(result).isTrue();
        }
    }
}
